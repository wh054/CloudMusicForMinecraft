# AGENT.md

## 项目概览

CloudMusicForMinecraft 是一个面向 Minecraft 1.12.2 的 Forge Mod，用音乐盒物品和音乐方块在游戏内播放网易云音乐。

核心信息：
- Mod id：`ncmfm`
- 入口类：`team.info.ncmfm.NcmMod`
- Java 目标版本：Java 8
- 构建系统：Gradle Wrapper，Gradle 4.9，ForgeGradle 3
- Forge 版本：`net.minecraftforge:forge:1.12.2-14.23.5.2854`
- 主要依赖：`jlayer`、`commons-io`、`httpclient`，以及 Minecraft/Forge classpath 内的 Gson

## 常用命令

使用仓库内的 Gradle Wrapper，不要依赖系统 Gradle。

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew genIntellijRuns
./gradlew genEclipseRuns
```

注意：
- `jar.finalizedBy('reobfJar')`：普通 jar 构建后会执行 Forge reobf。
- `shadowJar` 会打入 `com.badlogicgames.jlayer:jlayer:1.0.1-gdx`，并配置了 Forge reobf。
- 当前仓库没有 `src/test`，也没有配置单元测试框架。
- Gradle 4.9 / ForgeGradle 3 较旧；构建和 IDE 初始化优先使用 Java 8。

## 目录结构

```text
src/main/java/team/info/ncmfm/
  NcmMod.java                 Forge 入口、Mod 常量、网络包注册、代理生命周期
  NcmConfig.java              Forge 配置：网易云 API host、Cookie、码率
  proxy/                      客户端/服务端生命周期隔离
  eventHandler/               Forge 事件处理：注册、打开 UI、声音拦截、tick 计数
  item/                       `ItemMusicBox`
  block/                      `MusicCube`
  ui/                         Minecraft GUI 界面（含 `MusicPannel` 主面板、`QrLoginScreen` 扫码登录）
  component/                  GUI 滚动列表组件
  manager/                    网易云音乐 API 客户端和缓存
  net/                        SimpleNetworkWrapper 消息和处理器
  audio/                      Paulscode SoundSystem MP3 codec
  interfaces/                 `IProxy`、`IMusicManager`
  model/                      UI/网络层 DTO
  entity/                     网易云 API Gson 响应 DTO
  utils/                      MD5 等工具

src/main/resources/
  mcmod.info                  Forge 元数据
  pack.mcmeta                 资源包元数据
  assets/ncmfm/               贴图、blockstate、model、语言文件
```

## 架构要点

### Forge 生命周期

`NcmMod` 在 `preInit` 中注册两个 `MusicMessage` 处理器：
- discriminator `224`：发往 `Side.CLIENT`，处理器 `MusicMessageClientHandler`
- discriminator `223`：发往 `Side.SERVER`，处理器 `MusicMessageHandler`

生命周期逻辑通过 `IProxy` 分发：
- `ClientProxy` 注册方块/物品/渲染/玩家交互/声音事件，注册 MP3 codec，并在 `postInit` 里反射取得 Minecraft 的 `SoundSystem`。
- `ServerProxy` 只注册物品和方块注册事件。

客户端专用类必须留在客户端路径中。`Minecraft.getMinecraft()`、GUI、声音、`ModelLoader`、`@SideOnly(Side.CLIENT)` 相关逻辑不要引入服务端代码。

### 注册名和资源

当前注册名：
- 物品：`music_box`
- 方块：`music_cube`

如果修改 registry name 或 unlocalized name，必须同步更新 `src/main/resources/assets/ncmfm/` 下的模型、blockstate 和语言键。

资源使用 Minecraft 1.12 格式：
- `blockstates/music_cube.json`
- `models/block/music_cube.json`
- `models/item/music_box.json`
- `models/item/music_cube.json`
- `lang/en_us.lang`
- `lang/zh_cn.lang`
- `textures/...`

### 网易云音乐 API

`NeteaseCloudMusicManager` 实现 `IMusicManager`，通过 `NcmConfig.host` 访问兼容网易云音乐 API（NeteaseCloudMusicApi Enhanced）的服务。所有请求均为 GET。

当前使用的接口：
- `/login/status`：用 Cookie 校验登录态，取 `userId`
- `/login/refresh`：刷新登录态
- `/login/qr/key`：申请二维码 `unikey`
- `/login/qr/create?key=...&qrimg=true`：生成二维码，直接返回 base64 PNG（`qrimg`）和 `qrurl`
- `/login/qr/check?key=...`：轮询扫码状态（801 等待 / 802 待确认 / 803 成功 / 800 过期）
- `/user/playlist?uid=...`
- `/playlist/detail?id=...`
- `/album?id=...`
- `/album/sublist`
- `/personal_fm?timestamp=...`
- `/song/url/v1?id=...&level=...`

不要硬编码账号或 API host。玩家相关配置统一走 `NcmConfig`。

#### 登录方式（仅 Cookie + 扫码，已移除手机号密码登录）

- **Cookie**：`NcmConfig.cookie` 非空时，`login()` 用它调 `/login/status` 校验，成功即登录。
- **扫码**：`MusicPannel.initGui` 调 `login()` 后若 `isLoggedIn()` 为假，跳转 `QrLoginScreen`。
  界面在后台守护线程里 `createQrCode()` → 轮询 `checkQrStatus()`；过期自动重建二维码，
  成功（803）后由 `finishQrLogin` 完成登录，并把 Cookie 通过 `ConfigManager.sync` 回写 `NcmConfig.cookie`（best-effort），下次免扫码。
  二维码图片由 API 以 base64 PNG 返回，前端用 `DynamicTexture` 渲染，**无需引入二维码生成库**。

关键约束（改登录逻辑时务必遵守）：
- **Cookie 清洗**：扫码 `check`（803）在响应 body 的 `cookie` 字段返回的是原始 Set-Cookie 拼接串，
  含 `Max-Age/Expires/Path` 等属性段（`Expires` 内含逗号）。必须经 `sanitizeCookie` 只保留
  `name=value`（如 `MUSIC_U`、`__csrf`）后再用于请求头，否则 HttpClient 解析失败、登录态拿不到。
- HttpClient 用 `CookieSpecs.IGNORE_COOKIES` 关闭自带 cookie 解析（我们全程手动管理 Cookie header），
  避免 `Invalid cookie header` 告警刷屏。
- 网络请求只能在后台线程；`DynamicTexture` 上传和 `displayGuiScreen` 切换必须回主线程（见 `QrLoginScreen.updateScreen`）。

`NeteaseCloudMusicManager.cache` 是静态进程级缓存，用于 `userId`、Cookie、二维码 `unikey`、歌单、专辑和曲目。改登录、刷新或请求逻辑时，要同时考虑缓存状态和 Cookie header。

### 播放和网络同步

`MusicPannel` 将 `MusicInfoWrapper` 序列化成 JSON，塞进 `MusicMessage` 发给服务端。服务端 `MusicMessageHandler` 再广播给所有客户端。客户端 `MusicMessageClientHandler` 根据命令执行：
- `EnumMusicCommand.PLAY`：开始播放
- `EnumMusicCommand.STOP`：停止播放

播放模式：
- 无 `BlockPos`：背景立体声流，source 名为 `background.StereoMp3`
- 有 `BlockPos`：方块位置单声道流，source 名为 `MD5(pos.toString()) + ".MonoMp3"`

`GameSoundHandler` 会在背景音乐流播放时拦截原版游戏音乐。

## Agent 开发规则

- 保持 Java 8 语法和 Forge 1.12.2 API。
- 复用现有包结构和命名风格，不引入第二套架构。
- 修改注册名、mod id、网络 channel、资源路径时，必须同步所有调用点和资源文件。
- 服务端安全代码不能 import 客户端专用类。
- 可配置值走 `NcmConfig`；不要把用户凭据写进代码。
- 修改网络包时，同时检查 `MusicMessage` 序列化和两个 handler。
- 修改 API DTO 时，同时检查 `entity/` 与 `model/` 中被 Gson 和 UI 使用的字段。
- 修改 UI 选择逻辑时，严查列表边界；列表可能为空，也可能在点击时已刷新。
- 不保留无用兼容层、别名或重复约定；改动应一次性切到新路径。

## 验证清单

代码或构建改动先跑最小相关命令，再跑：

```bash
./gradlew build
```

行为改动还需要用客户端运行配置验证对应场景：
- 物品和方块能正常注册并出现在游戏内
- 右键 `music_box` 打开 `MusicPannel`
- 空手右键 `music_cube` 打开位置播放 UI
- 播放/停止能经服务端转发并影响客户端
- 仅在背景音乐流播放时拦截原版音乐
- Forge 配置中的 Cookie、API host 和码率能正确加载
- 未登录时打开面板会跳转 `QrLoginScreen` 并显示二维码；手机扫码确认后自动进入 `MusicPannel`，Cookie 回写配置；过期二维码能自动刷新；日志无 `Invalid cookie header` 刷屏

如果 Gradle 因旧 ForgeGradle/JDK 兼容性失败，记录准确命令和失败信息，并先切换 Java 8 重试，不要直接改业务代码绕过构建问题。
