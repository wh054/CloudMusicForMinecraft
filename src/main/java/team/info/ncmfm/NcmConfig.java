package team.info.ncmfm;

import net.minecraftforge.common.config.Config;

@Config(
        modid = NcmMod.MODID
)
public class NcmConfig {
    @Config.Comment("网易云 API Enhanced Host")
    public static String host="http://127.0.0.1:3000";
    @Config.Comment("网易云 Cookie（可留空，未登录时游戏内会弹出二维码扫码登录并自动回填）")
    public static String cookie="";
    @Config.Comment("码率")
    public static String bitRate="128000";
    @Config.Comment("是否在客户端启动时自动启动内嵌 API 服务（需要在 ncm-api 目录下放置 API 文件）")
    public static boolean autoStartApiServer = true;
    @Config.Comment("内嵌 API 服务端口")
    public static int apiServerPort = 3000;
    @Config.Comment("是否显示桌面歌词")
    public static boolean showLyrics = true;
}
