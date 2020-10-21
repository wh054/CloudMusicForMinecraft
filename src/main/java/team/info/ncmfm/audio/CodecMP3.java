package team.info.ncmfm.audio;

import javazoom.jl.decoder.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class CodecMP3 implements ICodec {
    private static final Logger Log = LogManager.getLogger(CodecMP3.class);
    private boolean initialized;
    private boolean streamClosed;

    private Bitstream bitstream;
    private MP3Decoder decoder;
    private AudioFormat audioFormat;
    private OutputBuffer buffer;

    @Override
    public void reverseByteOrder(boolean b) {}

    @Override
    public boolean initialize(URL url) {
        try {
            final URLConnection conn = url.openConnection();
            conn.connect();

            bitstream = new Bitstream(conn.getInputStream());
            decoder = new MP3Decoder();
            initialized = true;

            updateBuffer(); // get single frame here, to receive stream params
            audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16, 1, true, false);
            return true;
        } catch (Throwable t) {
            Log.warn("Failed to initalize codec for url '%s'");
        }

        return false;
    }

    private boolean updateBuffer() throws Exception {
        Header h = bitstream.readFrame();
        if (h == null)
            return false;
        if (buffer == null) {
            buffer = new OutputBuffer(h.mode() == Header.SINGLE_CHANNEL ? 1 : 2, false);
        } else {
            buffer.reset();
        }
        decoder.setOutputBuffer(buffer);
        decoder.decodeFrame(h, bitstream);
        bitstream.closeFrame();
        return true;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    @Override
    public SoundBuffer read() {
        if (!initialized || streamClosed)
            return null;

        final int limit = SoundSystemConfig.getStreamingBufferSize();
        ByteArrayOutputStream output = new ByteArrayOutputStream(limit);

        try {
            do {
                readBytes(output);
                if (!updateBuffer())
                    break;
            } while (!streamClosed && output.size() < limit);
        } catch (Throwable t) {
            Log.warn("Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), audioFormat);
    }

    @Override
    public SoundBuffer readAll() {
        if (!initialized || streamClosed)
            return null;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            do {
                readBytes(output);
                if (!updateBuffer())
                    break;
            } while (!streamClosed);
        } catch (Throwable t) {
            Log.warn("Error in stream decoding, aborting");
            streamClosed = true;
        }

        return new SoundBuffer(output.toByteArray(), audioFormat);
    }

    private void readBytes(OutputStream output) throws IOException {
        byte[] buff=covertMono(buffer.getBuffer());
        output.write(buff);
    }

    private byte[] covertMono(byte[] buff){
        byte[] mono=null;
        if(buff!= null ) {
            /** Convert to mono */
             mono = new byte[buff.length / 2];
            for (int i = 0; i < mono.length / 2; ++i) {
                int HI = 1;
                int LO = 0;
                int left = (buff[i * 4 + HI] << 8) | (buff[i * 4 + LO] & 0xff);
                int right = (buff[i * 4 + 2 + HI] << 8) | (buff[i * 4 + 2 + LO] & 0xff);
                int avg = (left + right) / 2;
                mono[i * 2 + HI] = (byte) ((avg >> 8) & 0xff);
                mono[i * 2 + LO] = (byte) (avg & 0xff);
            }
        }
        return mono;
    }

    @Override
    public boolean endOfStream() {
        return streamClosed;
    }

    @Override
    public void cleanup() {
        streamClosed = true;
        initialized = false;
        decoder = null;
        try {
            bitstream.close();
        } catch (BitstreamException e) {
            Log.warn("Failed to close bitstream");
        }
        bitstream = null;
        buffer = null;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

}
