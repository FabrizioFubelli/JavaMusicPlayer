package ulg.play.players;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Duration;
import javazoom.jlme.util.Player;
import ulg.play.media.Media;
import ulg.play.media.MediaPlayer;

import java.io.*;
import java.util.Objects;

import static ulg.play.media.MediaPlayer.PlayerType.NORMAL;

public class ClassicMediaPlayer implements MediaPlayer {

    private final PlayerType TYPE = NORMAL;
    private final String audioFile;

    private Player classicPlayer;
    private long startMillis;

    private final Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            if (getStatus() == Status.PLAYING) {
                try {
                    if (Objects.isNull(classicPlayer)) {
                        InputStream targetStream = new FileInputStream(new File(audioFile));
                        classicPlayer = new Player(targetStream);
                    }
                    startMillis = System.currentTimeMillis();
                    classicPlayer.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (getStatus() == Status.PAUSED) {
                // DA IMPLEMENTARE
            }
        }
    };

    private Thread playThread;

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
        this.setStatus(Status.PLAYING);
        try {
            if (Objects.isNull(this.playThread)) {
                this.playThread = new Thread(this.playRunnable);
                this.playThread.setDaemon(true);
            }
            this.playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void _playFrom(int second) {  // DA SISTEMARE
        startTime = second;
        this.setStatus(Status.PLAYING);
    }

    @Override
    public void _pause() {
        this.setStatus(Status.PAUSED);
    }

    @Override
    public void _stop() {
        this.setStatus(Status.STOPPED);
        if (!Objects.isNull(this.classicPlayer)) {
            this.playThread.stop();
            this.classicPlayer = null;
            this.playThread = null;
        }
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
        return Duration.millis(System.currentTimeMillis()-this.startMillis);
    }

    private int startTime = 0;

    @Override
    public Duration getStartTime() {
        return Duration.seconds(startTime);
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
