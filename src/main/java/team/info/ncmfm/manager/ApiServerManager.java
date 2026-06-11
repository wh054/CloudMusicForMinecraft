package team.info.ncmfm.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.NcmConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理内嵌的 NeteaseCloudMusicApi Enhanced 子进程。
 * <p>
 * 在客户端 preInit 时启动，游戏退出时通过 shutdown hook 关闭。
 * 支持两种模式：
 * <ul>
 *   <li>预编译二进制：{gameDir}/ncm-api/app.exe</li>
 *   <li>Node.js 源码：node {gameDir}/ncm-api/app.js</li>
 * </ul>
 */
public class ApiServerManager {
    private static final Logger logger = LogManager.getLogger(ApiServerManager.class);

    /** 子进程在游戏运行目录下的文件夹路径 */
    private static final String API_DIR_NAME = "ncm-api" + File.separator + "api-enhanced";
    /** 预编译二进制文件名 */
    private static final String BINARY_NAME = "app.exe";
    /** Node.js 入口文件名 */
    private static final String NODE_ENTRY = "app.js";
    /** 健康检查最大等待时间（毫秒） */
    private static final long HEALTH_CHECK_TIMEOUT_MS = 15_000;
    /** 健康检查轮询间隔（毫秒） */
    private static final long HEALTH_CHECK_INTERVAL_MS = 500;
    /** 关闭等待时间（毫秒） */
    private static final long SHUTDOWN_WAIT_MS = 5_000;

    private static volatile Process apiProcess;
    private static volatile boolean running = false;
    private static volatile Thread shutdownHook;

    /**
     * 启动 API 服务子进程。
     * <p>
     * 仅在以下条件全部满足时才会启动：
     * <ul>
     *   <li>NcmConfig.autoStartApiServer 为 true</li>
     *   <li>NcmConfig.host 指向 localhost</li>
     *   <li>能找到 API 可执行文件或 Node.js + app.js</li>
     * </ul>
     *
     * @param gameDir Minecraft 运行目录（通常为 .minecraft 或 run）
     */
    public static void start(File gameDir) {
        if (!NcmConfig.autoStartApiServer) {
            logger.info("Embedded API server auto-start is disabled in config.");
            return;
        }

        if (!isLocalHost(NcmConfig.host)) {
            logger.info("API host '{}' is not localhost, skipping embedded server start.", NcmConfig.host);
            return;
        }

        if (running && apiProcess != null && apiProcess.isAlive()) {
            logger.info("Embedded API server is already running.");
            return;
        }

        final File apiDir = new File(gameDir, API_DIR_NAME);

        Thread startThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doStart(apiDir);
                } catch (Exception e) {
                    logger.error("Failed to start embedded API server: " + e.getMessage(), e);
                }
            }
        }, "ncmfm-api-starter");
        startThread.setDaemon(true);
        startThread.start();
    }

    /**
     * 停止 API 服务子进程。
     */
    public static void stop() {
        Process proc = apiProcess;
        if (proc == null) {
            return;
        }

        logger.info("Stopping embedded API server...");
        running = false;

        proc.destroy();
        try {
            long deadline = System.currentTimeMillis() + SHUTDOWN_WAIT_MS;
            while (proc.isAlive() && System.currentTimeMillis() < deadline) {
                Thread.sleep(200);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        if (proc.isAlive()) {
            logger.warn("API server did not exit gracefully, force killing.");
            proc.destroyForcibly();
        }

        apiProcess = null;
        logger.info("Embedded API server stopped.");

        // 移除 shutdown hook（如果不是从 hook 中调用的话）
        removeShutdownHook();
    }

    /**
     * 检查 API 服务是否正在运行。
     */
    public static boolean isRunning() {
        return running && apiProcess != null && apiProcess.isAlive();
    }

    // ===== 内部实现 =====

    private static void doStart(File apiDir) {
        if (!apiDir.isDirectory()) {
            logger.warn("API directory not found: {}. Embedded API server will not start.", apiDir.getAbsolutePath());
            logger.warn("To use the embedded API server, place the API files in: {}", apiDir.getAbsolutePath());
            return;
        }

        List<String> command = buildCommand(apiDir);
        if (command == null) {
            logger.warn("No API executable found in {}. Embedded API server will not start.", apiDir.getAbsolutePath());
            logger.warn("Place app.exe (pre-compiled) or app.js + Node.js in the ncm-api directory.");
            return;
        }

        int port = NcmConfig.apiServerPort;
        logger.info("Starting embedded API server: {} (port {})", joinCommand(command), port);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(apiDir);
            pb.redirectErrorStream(true);

            Map<String, String> env = pb.environment();
            env.put("PORT", String.valueOf(port));
            // 阻止 Node.js 弹出浏览器
            env.put("BROWSER", "none");

            apiProcess = pb.start();
            running = true;

            // 注册 shutdown hook
            registerShutdownHook();

            // 启动日志转发线程
            startLogForwarder(apiProcess);

            // 健康检查
            if (waitForReady(port)) {
                logger.info("Embedded API server is ready on port {}.", port);
            } else {
                logger.warn("API server health check timed out after {}ms. The server may still be starting.",
                        HEALTH_CHECK_TIMEOUT_MS);
            }
        } catch (Exception e) {
            logger.error("Failed to start API server process: " + e.getMessage(), e);
            running = false;
        }
    }

    /**
     * 构建启动命令。优先使用预编译二进制，其次使用 Node.js。
     */
    private static List<String> buildCommand(File apiDir) {
        // 优先：预编译二进制
        File binary = new File(apiDir, BINARY_NAME);
        if (binary.isFile()) {
            List<String> cmd = new ArrayList<String>();
            cmd.add(binary.getAbsolutePath());
            return cmd;
        }

        // 其次：Node.js + app.js
        File entryFile = new File(apiDir, NODE_ENTRY);
        if (entryFile.isFile()) {
            String nodeExe = findNodeExecutable();
            if (nodeExe != null) {
                File nodeModules = new File(apiDir, "node_modules");
                if (!nodeModules.isDirectory()) {
                    installDependencies(apiDir);
                }
                List<String> cmd = new ArrayList<String>();
                cmd.add(nodeExe);
                cmd.add(entryFile.getAbsolutePath());
                return cmd;
            }
            logger.warn("Found {} but Node.js is not installed or not in PATH.", entryFile.getAbsolutePath());
        }

        return null;
    }

    /**
     * 自动安装 Node.js 项目依赖。
     */
    private static boolean installDependencies(File apiDir) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String installer = "npm";
        if (new File(apiDir, "pnpm-lock.yaml").exists() && isCommandAvailable("pnpm", isWindows)) {
            installer = "pnpm";
        } else if (new File(apiDir, "yarn.lock").exists() && isCommandAvailable("yarn", isWindows)) {
            installer = "yarn";
        }

        String execName = isWindows ? installer + ".cmd" : installer;
        logger.info("node_modules not found in {}. Installing dependencies using '{}'...", apiDir.getAbsolutePath(), installer);

        try {
            ProcessBuilder pb = new ProcessBuilder(execName, "install");
            pb.directory(apiDir);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[ncm-api-install] {}", line);
            }
            int code = p.waitFor();
            if (code == 0) {
                logger.info("Successfully installed dependencies using '{}'.", installer);
                return true;
            } else {
                logger.error("Dependency installation failed with exit code {}.", code);
            }
        } catch (Exception e) {
            logger.error("Failed to run dependency installer '" + execName + "': " + e.getMessage(), e);
            if (!"npm".equals(installer)) {
                logger.info("Falling back to npm install...");
                try {
                    String fallbackExec = isWindows ? "npm.cmd" : "npm";
                    ProcessBuilder pb = new ProcessBuilder(fallbackExec, "install");
                    pb.directory(apiDir);
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("[ncm-api-install] {}", line);
                    }
                    int code = p.waitFor();
                    if (code == 0) {
                        logger.info("Successfully installed dependencies using fallback npm.");
                        return true;
                    }
                } catch (Exception ex) {
                    logger.error("Failed to run fallback npm: " + ex.getMessage(), ex);
                }
            }
        }
        return false;
    }

    private static boolean isCommandAvailable(String cmd, boolean isWindows) {
        try {
            String checkCmd = isWindows ? cmd + ".cmd" : cmd;
            ProcessBuilder pb = new ProcessBuilder(checkCmd, "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 在 PATH 中查找 Node.js 可执行文件。
     */
    private static String findNodeExecutable() {
        // Windows: 先找 node.exe
        String[] candidates = {"node.exe", "node"};
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }

        for (String candidate : candidates) {
            for (String dir : pathEnv.split(File.pathSeparator)) {
                File file = new File(dir, candidate);
                if (file.isFile() && file.canExecute()) {
                    return file.getAbsolutePath();
                }
            }
        }

        // 也尝试直接运行 "node" 让 OS 自己找
        try {
            ProcessBuilder test = new ProcessBuilder("node", "--version");
            test.redirectErrorStream(true);
            Process p = test.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String version = reader.readLine();
            int code = p.waitFor();
            if (code == 0 && version != null && version.startsWith("v")) {
                logger.info("Found Node.js: {}", version);
                return "node";
            }
        } catch (Exception ignored) {
            // Node.js 不可用
        }

        return null;
    }

    /**
     * 启动守护线程读取子进程输出并转发到 Minecraft 日志。
     */
    private static void startLogForwarder(final Process process) {
        Thread logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("[ncm-api] {}", line);
                    }
                } catch (Exception e) {
                    if (running) {
                        logger.debug("API server log forwarder ended: {}", e.getMessage());
                    }
                }
            }
        }, "ncmfm-api-log");
        logThread.setDaemon(true);
        logThread.start();
    }

    /**
     * 轮询 API 服务直到就绪或超时。
     */
    private static boolean waitForReady(int port) {
        long deadline = System.currentTimeMillis() + HEALTH_CHECK_TIMEOUT_MS;
        String healthUrl = "http://127.0.0.1:" + port + "/";

        while (System.currentTimeMillis() < deadline) {
            // 如果进程已退出，立即返回失败
            Process proc = apiProcess;
            if (proc == null || !proc.isAlive()) {
                logger.error("API server process exited unexpectedly.");
                running = false;
                return false;
            }

            try {
                URL url = new URL(healthUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                conn.setRequestMethod("GET");
                int statusCode = conn.getResponseCode();
                conn.disconnect();
                if (statusCode >= 200 && statusCode < 500) {
                    return true;
                }
            } catch (Exception ignored) {
                // 服务尚未就绪，继续等待
            }

            try {
                Thread.sleep(HEALTH_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 判断 host 是否指向本机。
     */
    private static boolean isLocalHost(String host) {
        if (host == null || host.trim().length() == 0) {
            return true; // 空的默认视为本地
        }
        String lower = host.trim().toLowerCase();
        try {
            URI uri = new URI(lower);
            String uriHost = uri.getHost();
            if (uriHost != null) {
                return "localhost".equals(uriHost) || "127.0.0.1".equals(uriHost) || "0.0.0.0".equals(uriHost);
            }
        } catch (Exception ignored) {
        }
        return lower.contains("localhost") || lower.contains("127.0.0.1");
    }

    private static void registerShutdownHook() {
        if (shutdownHook != null) {
            return;
        }
        shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                // 在 shutdown hook 中，不需要再 removeShutdownHook
                Process proc = apiProcess;
                if (proc != null && proc.isAlive()) {
                    logger.info("Shutdown hook: stopping embedded API server...");
                    proc.destroy();
                    try {
                        long deadline = System.currentTimeMillis() + SHUTDOWN_WAIT_MS;
                        while (proc.isAlive() && System.currentTimeMillis() < deadline) {
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException ignored) {
                    }
                    if (proc.isAlive()) {
                        proc.destroyForcibly();
                    }
                }
            }
        }, "ncmfm-api-shutdown");
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // JVM 已经在关闭
        }
    }

    private static void removeShutdownHook() {
        Thread hook = shutdownHook;
        if (hook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException ignored) {
                // JVM 正在关闭
            }
            shutdownHook = null;
        }
    }

    private static String joinCommand(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(command.get(i));
        }
        return sb.toString();
    }
}
