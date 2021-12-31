package team.info.ncmfm;

import net.minecraftforge.common.config.Config;

@Config(
        modid = NcmMod.MODID
)
public class NcmConfig {
    @Config.Comment("手机号")
    public static String phone="test";
    @Config.Comment("密码")
    public static String password="test";
    @Config.Comment("云音乐API Host")
    public static String host="test";
    @Config.Comment("码率")
    public static String bitRate="128000";
}
