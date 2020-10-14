package team.info.ncmfm.audio;

public class Mp3Player extends MediaPlayer implements Runnable{

    public Mp3Player(String url) {
        super(url);
    }

    @Override
    public void run() {
        super.play();
    }
}
