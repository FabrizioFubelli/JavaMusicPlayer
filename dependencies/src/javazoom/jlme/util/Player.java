package javazoom.jlme.util;


import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.InputStream;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;
import javazoom.jlme.decoder.BitStream;


public class Player {

    private enum Status {
        PLAY,
        PAUSE,
        STOP
    }

    private static Status status;
    private static SourceDataLine line;
    private BitStream bitstream;
    private boolean playable = true;

    public Player(InputStream stream) throws Exception {
        bitstream = new BitStream(stream);
    }

    private static void startOutput(AudioFormat playFormat) throws LineUnavailableException {
        DataLine.Info info= new DataLine.Info(SourceDataLine.class, playFormat);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("sorry, the sound format cannot be played");
        }
        line = (SourceDataLine)AudioSystem.getLine(info);
        line.open(playFormat);
        line.start();
    }

    private static void stopOutput() {
        if (line != null)
        {
            try {
                line.drain();
                line.stop();
                line.close();
                line = null;
            } catch (NullPointerException e) {
                // skip
            }
        }
    }

    public void resume() {
        Status old_status = status;
        status = Status.PLAY;
        line.start();
        if (old_status == Status.PAUSE) {
            synchronized (this) {
                this.notify();
            }
        }
        System.out.println("resume");
    }

    public void pause() {
        status = Status.PAUSE;
        line.stop();
        System.out.println("pause");
    }

    public void play() throws Exception {
        status = Status.PLAY;
        boolean first = true;
        int length;
        Header header = bitstream.readFrame();
        Decoder decoder = new Decoder(header, bitstream);
        System.out.println("play");
        while (playable)
        {
            if (status == Status.PAUSE) {
                synchronized (this) {
                    this.wait();
                }
            }
            try
            {
                SampleBuffer output = decoder.decodeFrame();
                length = output.size();
                if (length == 0) break;
                //{
                if (first)
                {
                    first = false;
                    System.out.println("frequency: "+ decoder.getOutputFrequency() + ", channels: " + decoder.getOutputChannels());
                    startOutput(new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false));
                }
                line.write(output.getBuffer(), 0, length);
                bitstream.closeFrame();
                bitstream.readFrame();
            } catch (Exception e)
            {
                break;
            }
        }
        playable = false;
        stopOutput();
        bitstream.close();
    }

    public void stop() {
        status = Status.STOP;
        playable = false;
        System.out.println("stop");
    }
}
