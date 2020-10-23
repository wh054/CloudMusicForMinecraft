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

public class CodecStereoMp3 implements ICodec {
    private static final Logger Log = LogManager.getLogger(CodecStereoMp3.class);
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
            audioFormat = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
            return true;
        } catch (Throwable t) {
            Log.warn("Failed to initalize codec for url");
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
        output.write(buffer.getBuffer());
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
