# 本地运行排障记录

记录时间：2026-06-11  
项目：CloudMusicForMinecraft  
目标：让 Forge 1.12.2 开发环境在本机完成 `build` 并启动 `runClient`

## 本机环境

- Windows PowerShell
- JDK 8：`C:\Program Files\Java\jdk1.8.0_181`
- Gradle Wrapper：Gradle 4.9
- 本地代理：`127.0.0.1:7890`
- Forge：`1.12.2-14.23.5.2854`
- Mappings：`snapshot_20171003-1.12`

## 推荐运行命令

每次开新 PowerShell 后，先设置 JDK 8 和代理：

```powershell
$proxy='-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Djava.net.preferIPv4Stack=true -Dsun.net.client.defaultConnectTimeout=60000 -Dsun.net.client.defaultReadTimeout=60000'
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_181'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:JAVA_OPTS=$proxy
$env:GRADLE_OPTS=$proxy
```

构建：

```powershell
.\gradlew.bat build --console=plain
```

启动客户端：

```powershell
.\gradlew.bat runClient --console=plain
```

构建产物：

```text
build\libs\NcmMod-2.2.jar
```

## 问题 1：默认 Java 版本过高

### 现象

本机默认 `java -version` 是较新的 JDK 25。Forge 1.12.2、Gradle 4.9 和旧版 ForgeGradle 组合更适合 Java 8，使用新 JDK 容易触发 Gradle/Forge 工具链兼容性问题。

### 处理

不要改全局 Java，直接在当前 PowerShell 会话中设置：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_181'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

验证：

```powershell
java -version
```

期望看到 `1.8.0_181`。

## 问题 2：Gradle 下载依赖需要走本地代理

### 现象

首次运行 Gradle Wrapper 或下载 Forge/Minecraft 依赖时，可能出现下载缓慢、卡住或超时。

### 处理

Gradle Wrapper 本身也是 Java 进程，所以同时设置 `JAVA_OPTS` 和 `GRADLE_OPTS`：

```powershell
$proxy='-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Djava.net.preferIPv4Stack=true'
$env:JAVA_OPTS=$proxy
$env:GRADLE_OPTS=$proxy
```

如果远端偶发慢，附加超时参数：

```powershell
-Dsun.net.client.defaultConnectTimeout=60000 -Dsun.net.client.defaultReadTimeout=60000
```

## 问题 3：Gradle Wrapper 下载中断后的本地缓存

### 现象

首次下载 Gradle 4.9 时，如果进程卡住或被中断，`~\.gradle\wrapper\dists` 下可能留下不完整缓存，后续继续卡在 wrapper 解压或下载阶段。

### 处理

只在确认没有正在使用的 Java/Gradle 进程后，删除对应的 Gradle 4.9 wrapper 缓存目录。

本次清理过的目录：

```text
C:\Users\wsp39\.gradle\wrapper\dists\gradle-4.9-all\491wbe0x5d54n9cojs2p0zv90
```

注意：删除前要确认路径在 `C:\Users\wsp39\.gradle\wrapper\dists` 下，避免误删其他目录。

## 问题 4：Java 编译输出 GBK 不可映射字符

### 现象

构建能成功，但 `compileJava` 阶段大量输出类似：

```text
错误: 编码GBK的不可映射字符
```

触发点是源码注释里有 UTF-8 内容，而 Windows/Gradle 默认用 GBK 编译。

### 处理

已在 `build.gradle` 固化：

```gradle
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}
```

## 问题 5：`runClient` 在 FML 初始化时 NPE

### 现象

`runClient` 崩溃，crash report 中关键栈如下：

```text
net.minecraftforge.fml.common.LoaderExceptionModCrash: Caught exception from Forge Mod Loader (FML)
Caused by: java.lang.NullPointerException
    at net.minecraftforge.fml.common.network.NetworkRegistry.newChannel(NetworkRegistry.java:207)
    at net.minecraftforge.fml.common.network.internal.FMLNetworkHandler.registerChannel(FMLNetworkHandler.java:185)
    at net.minecraftforge.fml.common.FMLContainer.modConstruction(FMLContainer.java:92)
```

本次对应的旧 crash report：

```text
run\crash-reports\crash-2026-06-11_21.45.32-client.txt
run\crash-reports\crash-2026-06-11_21.52.03-client.txt
```

### 根因

ForgeGradle 生成的本地 Forge jar 中，`net.minecraftforge.fml.relauncher.Side` 被加入了额外枚举值：

```text
CLIENT
SERVER
BUKKIT
```

但 Forge 1.12.2 的 `NetworkRegistry` 构造函数只为 `CLIENT` 和 `SERVER` 初始化 channel map。后续 `newChannel` 遍历 `Side.values()` 时遇到 `BUKKIT`，`channels.get(side)` 返回 `null`，于是触发 NPE。

这个问题与 ForgeGradle 的旧版本运行任务兼容性有关，社区也有相同记录：  
https://github.com/MinecraftForge/ForgeGradle/issues/748

### 验证根因的命令

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_181'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$jar='C:\Users\wsp39\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.12.2-14.23.5.2854_mapped_snapshot_20171003-1.12\forge-1.12.2-14.23.5.2854_mapped_snapshot_20171003-1.12.jar'
javap -classpath $jar -p net.minecraftforge.fml.relauncher.Side
```

修复前会看到 `BUKKIT`，修复后只应看到 `CLIENT` 和 `SERVER`。

### 已固化的处理

`build.gradle` 中新增了 `patchForgeSideEnum` 任务：

- 使用 `net.minecraftforge:mergetool:0.2.3.3:forge` 里的正确 `Side.class` 和 `SideOnly.class`
- 覆盖 ForgeGradle 本地生成的 Forge jars
- `build` 后自动执行
- `runClient` / `runServer` 前自动执行

关键任务顺序可用 dry-run 验证：

```powershell
.\gradlew.bat runClient --dry-run --console=plain
```

期望顺序中包含：

```text
:prepareRunClient
:patchForgeSideEnum
:runClient
```

## 问题 6：`mergetool` 进入运行时 classpath 后触发 ASM 扫描噪音

### 现象

如果把 `mergetool` 直接作为普通依赖加入运行时 classpath，FML 会扫描它的传递依赖 `asm-6.0`、`asm-tree-6.0`、`asm-util-6.0`，并输出：

```text
There was a problem reading the entry module-info.class
Zip file asm-6.0.jar failed to read properly, it will be ignored
```

### 处理

已在 `build.gradle` 中把补丁依赖放进独立配置，并从运行时排除：

```gradle
configurations {
    forgeSidePatch
    runtimeClasspath {
        exclude group: 'net.minecraftforge', module: 'mergetool'
    }
}

dependencies {
    forgeSidePatch('net.minecraftforge:mergetool:0.2.3.3:forge') {
        transitive = false
    }
}
```

这样 `mergetool` 只用于修补 ForgeGradle 生成物，不进入 Minecraft/FML 的运行时扫描范围。

## 最终验证结果

本次最终确认：

```text
.\gradlew.bat build --console=plain
BUILD SUCCESSFUL
```

`runClient` 日志中确认补丁任务执行：

```text
> Task :patchForgeSideEnum
Patched Forge side enum in ...forge-1.12.2-14.23.5.2854_mapped_snapshot_20171003-1.12.jar
```

`runClient` 日志中确认 FML 加载完成：

```text
Forge Mod Loader has successfully loaded 5 mods
```

未生成新的 crash report。旧 crash report 停留在修复前的：

```text
2026-06-11 21:45:32
2026-06-11 21:52:03
```

## 如果需要从头清理 ForgeGradle 生成缓存

只有在 `Side.BUKKIT` 问题重新出现、或怀疑 ForgeGradle 生成缓存损坏时才需要清理。

先确认没有 Gradle/Minecraft Java 进程正在运行，再删除：

```text
C:\Users\wsp39\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.12.2-14.23.5.2854
C:\Users\wsp39\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.12.2-14.23.5.2854_mapped_snapshot_20171003-1.12
```

然后重新执行：

```powershell
.\gradlew.bat build --console=plain
.\gradlew.bat runClient --console=plain
```
