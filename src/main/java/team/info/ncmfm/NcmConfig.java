package team.info.ncmfm;

import net.minecraftforge.common.config.Config;

@Config(
        modid = NcmMod.MODID
)
public class NcmConfig {
    @Config.Comment("�ֻ���")
    public static String phone="test";
    @Config.Comment("����")
    public static String password="test";
    @Config.Comment("������API Host")
    public static String host="test";
    @Config.Comment("����")
    public static String bitRate="128000";
}
