package team.info.ncmfm.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class Mp3Player extends MediaPlayer implements Runnable{
    private int contentLength=0;

    public Mp3Player(InputStream stream,int contentLength) {
        super(stream);
        this.contentLength=contentLength;
    }


    public int getLength() throws BitstreamException {
        BufferedInputStream buffer = new BufferedInputStream(super.mediaStream);
        Bitstream bt = new Bitstream(buffer);
        Header header = bt.readFrame();
        int mp3Length = this.contentLength;
        int time = (int) header.total_ms(mp3Length);

        return time/1000;
    }

    @Override
    public void run() {
        try {
            super.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }
}
