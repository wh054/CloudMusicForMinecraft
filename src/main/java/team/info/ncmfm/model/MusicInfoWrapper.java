package team.info.ncmfm.model;

import net.minecraft.util.math.BlockPos;
import team.info.ncmfm.net.EnumMusicCommand;

import java.io.Serializable;

public class MusicInfoWrapper implements Serializable {
    private EnumMusicCommand Command;
    private BlockPos Pos;
    private String Source;

    public EnumMusicCommand getCommand() {
        return Command;
    }

    public void setCommand(EnumMusicCommand command) {
        Command = command;
    }

    public BlockPos getPos() {
        return Pos;
    }

    public void setPos(BlockPos pos) {
        this.Pos = pos;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }
}
