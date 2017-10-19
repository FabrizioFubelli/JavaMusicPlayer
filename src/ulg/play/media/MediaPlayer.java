package ulg.play.media;

import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by: Fabrizio Fubelli
 * Date: 13/01/2017.
 */
public interface MediaPlayer {

    /**
     * Enumeration describing the different status values of a {@link MediaPlayer}.
     *
     * The principal <code>MediaPlayer</code> status transitions are given in the
     * following table:
     * <table border="1" summary="MediaPlayer status transition table">
     * <tr>
     * <th>Current \ Next</th><th>READY</th><th>PAUSED</th>
     * <th>PLAYING</th><th>STALLED</th><th>STOPPED</th>
     * </tr>
     * <tr>
     * <td><b>UNKNOWN</b></td><td>pre-roll</td><td></td><td></td><td></td><td></td>
     * </tr>
     * <tr>
     * <td><b>READY</b><td></td></td><td></td><td>autoplay; play()</td><td></td><td></td>
     * </tr>
     * <tr>
     * <td><b>PAUSED</b><td></td></td><td></td><td>play()</td><td></td><td>stop()</td>
     * </tr>
     * <tr>
     * <td><b>PLAYING</b><td></td></td><td>pause()</td><td></td><td>buffering data</td><td>stop()</td>
     * </tr>
     * <tr>
     * <td><b>STALLED</b><td></td></td><td>pause()</td><td>data buffered</td><td></td><td>stop()</td>
     * </tr>
     * <tr>
     * <td><b>STOPPED</b><td></td></td><td>pause()</td><td>play()</td><td></td><td></td>
     * </tr>
     * </table>
     * </p>
     * <p>The table rows represent the current state of the player and the columns
     * the next state of the player. The cell at the intersection of a given row
     * and column lists the events which can cause a transition from the row
     * state to the column state. An empty cell represents an impossible transition.
     * The transitions to <code>UNKNOWN</code> and to and from <code>HALTED</code>
     * status are intentionally not tabulated. <code>UNKNOWN</code> is the initial
     * status of the player before the media source is pre-rolled and cannot be
     * entered once exited. <code>HALTED</code> is a terminal status entered when
     * an error occurs and may be transitioned into from any other status but not
     * exited.
     * </p>
     * <p>
     * The principal <code>MediaPlayer</code> status values and transitions are
     * depicted in the following diagram:
     * <br/><br/>
     * <img src="doc-files/mediaplayerstatus.png" alt="MediaPlayer status diagram"/>
     * </p>
     * <p>
     * Reaching the end of the media (or the stopTime if this is defined)
     * while playing does not cause the status to change
     * from <code>PLAYING</code>. Therefore, for example, if
     * the media is played to its end and then a manual seek to an earlier
     * time within the media is performed, playing will continue from the
     * new media time.
     * </p>
     * @since JavaFX 2.0
     */
    enum Status {
        /**
         * State of the player immediately after creation. While in this state,
         * property values are not reliable and should not be considered.
         * Additionally, commands sent to the player while in this state will be
         * buffered until the media is fully loaded and ready to play.
         */
        STARTING,
        /**
         * Unknown state
         */
        UNKNOWN,
        /**
         * State of the player once it is prepared to play.
         * This state is entered only once when the movie is loaded and pre-rolled.
         */
        READY,
        /**
         * State of the player when playback is paused. Requesting the player
         * to play again will cause it to continue where it left off.
         */
        PAUSED,
        /**
         * State of the player when it is currently playing.
         */
        PLAYING,
        /**
         * State of the player when playback has stopped.  Requesting the player
         * to play again will cause it to start playback from the beginning.
         */
        STOPPED,
        /**
         * State of the player when data coming into the buffer has slowed or
         * stopped and the playback buffer does not have enough data to continue
         * playing. Playback will continue automatically when enough data are
         * buffered to resume playback. If paused or stopped in this state, then
         * buffering will continue but playback will not resume automatically
         * when sufficient data are buffered.
         */
        STALLED,
        /**
         * State of the player when a critical error has occurred.  This state
         * indicates playback can never continue again with this player.  The
         * player is no longer functional and a new player should be created.
         */
        HALTED,
        /**
         * State of the player after dispose() method is invoked. This state indicates
         * player is disposed, all resources are free and player SHOULD NOT be used again.
         * <code>Media</code> and <code>MediaView</code> objects associated with disposed player can be reused.
         * @since JavaFX 8.0
         */
        DISPOSED
    }

    enum PlayerType {
        FLAC,
        NORMAL
    }

    AtomicInteger SONG_ID = new AtomicInteger(0);
    String FACTORY_NAME = "Song-Thread-";
    ThreadFactory THREAD_FACTORY_SONGS = r -> new Thread(r, FACTORY_NAME+SONG_ID.getAndIncrement());
    ExecutorService RUNNING_SONGS = Executors.newFixedThreadPool(1, THREAD_FACTORY_SONGS);

    PlayerType getType();

    String getFile();

    /**
     * Starts playing the media. If previously paused, then playback resumes
     * where it was paused. If playback was stopped, playback starts
     * from the startTime. When playing actually starts the
     * status will be set to {@link Status#PLAYING}.
     */
    void _play();

    /**
     * Start the audio from the selected second
     * @param second the start point
     */
    void _playFrom(int second);

    /**
     * Pauses the player. Once the player is actually paused the status
     * will be set to {@link Status#PAUSED}.
     */
    void _pause();

    /**
     * Stops playing the media. This operation resets playback to startTime, and resets
     * currentCount to zero. Once the player is actually
     * stopped, the status will be set to {@link Status#STOPPED}. The
     * only transitions out of <code>STOPPED</code> status are to
     * {@link Status#PAUSED} and {@link Status#PLAYING} which occur after
     * invoking {@link #_pause()} or {@link #_play()}, respectively.
     * While stopped, the player will not respond to playback position changes
     * requested by Duration.
     */
    void _stop();

    /**
     * Sets the audio playback volume. Its effect will be clamped to the range
     * <code>[0.0,&nbsp;1.0]</code>.
     *
     * @param value the volume
     */
    void setVolume(double value);

    /**
     * Retrieves the audio playback volume. The default value is <code>1.0</code>.
     * @return the audio volume
     */
    double getVolume();

    /**
     * Sets the audio balance. Its effect will be clamped to the range
     * <code>[-1.0,&nbsp;1.0]</code>.
     * @param value the balance
     */
    void setBalance(double value);

    /**
     * Retrieves the audio balance.
     * @return the audio balance
     */
    double getBalance();

    /**
     * Retrieves the start time. The default value is <code>Duration.ZERO</code>.
     * @return the start time
     */
    Duration getCurrentTime();

    /**
     * Retrieves the start time. The default value is <code>Duration.ZERO</code>.
     * @return the start time
     */
    Duration getStartTime();

    /**
     * Retrieves the current player status.
     * @return the playback status
     */
    Status getStatus();

    /**
     * mute the audio
     * @param value the <code>mute</code> setting
     */
    void setMute (boolean value);

    /**
     * Retrieves the mute value.
     * @return the mute setting
     */
    boolean isMute();

    /**
     * Sets the end of media event handler.
     * @param value the event handler or <code>null</code>.
     */
    void setOnEndOfMedia(Runnable value);
}
