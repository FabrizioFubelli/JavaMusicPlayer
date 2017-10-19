package ulg.play.players;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Duration;
import ulg.play.media.Media;
import ulg.play.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Objects;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import static ulg.play.media.MediaPlayer.PlayerType.NORMAL;

public class ClassicMediaPlayer extends Thread implements MediaPlayer {
    private final PlayerType TYPE = NORMAL;
    //private final List<FLACDecoder> decoders = new ArrayList<>();
    private final String audioFile;

    protected static final boolean DEBUG = false;

    private AudioStream as;
    private AudioPlayer p;
    private boolean playback;

    //private Player flacPlayer;

    /*
     * The parent {@link FLACDecoder} object; read-only.
     *
     * @see FLACDecoder
     */
    //private FLACDecoder flacDecoder;

    public ClassicMediaPlayer(File audioFile) {
        if (null == audioFile) throw new NullPointerException("media == null!");
        this.audioFile = audioFile.toString();
    }


    /**
     * Retrieves the {@link Media} instance being played.
     * @return the <code>Media</code> object.
     *
    public final FLACDecoder getFlacDecoder() {
        return this.flacDecoder;
    }*/

    @Override
    public PlayerType getType() {
        return this.TYPE;
    }

    @Override
    public String getFile() {
        return this.audioFile;
    }

    @Override
    public void _play() {
        try {
            this.setStatus(Status.PLAYING);
            playback = true;
            setRandom();
            p.player.start(as);
            try {
                do {
                } while (as.available() > 0 && playback);
                if (playback) {
                    _play();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRandom() {
        try {
            System.out.println("Now Playing: " + this.audioFile);
            as = new AudioStream(new FileInputStream(this.audioFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void _playFrom(int second) {  // DA SISTEMARE
        startTime = second;
        /*try {
            //flacDecoder = new Player(RUNNING_SONGS).decode(flacFile);
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }*/
        //flacDecoder.play();
    }

    @Override
    public void _pause() {
        //System.out.println("FlacMediaPlayer -> pause() called (song = "+flacFile);
        this.setStatus(Status.PAUSED);
        //flacDecoder.pause();
    }

    @Override
    public void _stop() {
        //System.out.println("FlacMediaPlayer -> stop() called (song = "+flacFile);
        this.setStatus(Status.STOPPED);
        //decoders.forEach(FLACDecoder::stop);
        //decoders.clear();
        //flacDecoder = null;
        /*
        if (!STOP_CALLED_FOR_ALL.get()) {
            STOP_CALLED_FOR_ALL.set(true);
            for (FlacMediaPlayer f : ALL_FLAC_MEDIA_PLAYER) f.stop();
            STOP_CALLED_FOR_ALL.set(false);
        }*/

        //ALL_FLAC_MEDIA_PLAYER.forEach(FlacMediaPlayer::stop);
    }

    /**
     * The volume at which the media should be played. The range of effective
     * values is <code>[0.0&nbsp;1.0]</code> where <code>0.0</code> is inaudible
     * and <code>1.0</code> is full volume, which is the default.
     */
    private DoubleProperty volume;

    @Override
    public void setVolume(double value) {

    }

    @Override
    public double getVolume() {
        return 0;
    }

    /**
     * The balance, or left-right setting, of the audio output. The range of
     * effective values is <code>[-1.0,&nbsp;1.0]</code> with <code>-1.0</code>
     * being full left, <code>0.0</code> center, and <code>1.0</code> full right.
     * The default value is <code>0.0</code>.
     */
    private DoubleProperty balance;

    @Override
    public void setBalance(double value) {
    }

    @Override
    public double getBalance() {
        return 0;
    }

    /**
     * The current state of the flac song FlacMediaPlayer.
     */
    private ReadOnlyObjectWrapper<Status> status = new ReadOnlyObjectWrapper<>(Status.STARTING);

    private void setStatus(Status value) { status.set(value); }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public Duration getCurrentTime() {
        return null; //flacDecoder.getCurrentTime();
    }

    private int startTime = 0;

    @Override
    public Duration getStartTime() {
        return new Duration(startTime*1000);
    }

    /**
     * Whether the player audio is muted. A value of <code>true</code> indicates
     * that audio is <i>not</i> being produced. The value of this property has
     * no effect on volume, i.e., if the audio is muted and then
     * un-muted, audio playback will resume at the same audible level provided
     * of course that the <code>volume</code> property has not been modified
     * meanwhile. The default value is <code>false</code>.
     * @see #volume
     */
    private BooleanProperty mute;

    @Override
    public void setMute(boolean value) {

    }

    @Override
    public boolean isMute() {
        return mute.get();
    }

    private Runnable end;

    @Override
    public void setOnEndOfMedia(Runnable value) {
        end = value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.audioFile);
    }

    @Override
    public boolean equals(Object o) {
        return !Objects.isNull(o) && o.getClass() == getClass() &&
                Objects.equals(((ClassicMediaPlayer) o).audioFile, this.audioFile);
    }



}
