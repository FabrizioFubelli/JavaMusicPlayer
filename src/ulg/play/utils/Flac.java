package ulg.play.utils;

import org.jflac.apps.Player;
import ulg.play.media.Media;
import ulg.play.media.MediaPlayer;
import ulg.play.players.AudioMediaPlayer;
import ulg.play.players.FlacMediaPlayer;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by: Fabrizio Fubelli
 * Date: 08/01/2017.
 */
public class Flac {

    private final static AtomicInteger IDENTIFIER = new AtomicInteger(0);
    private final int ID;

    private volatile MediaPlayer player;

    public Flac(File flacFile) {
        this.ID = IDENTIFIER.getAndIncrement();
        if (canReadFlac(flacFile)) {
            if (Thread.currentThread().isInterrupted()) return;
            player = new FlacMediaPlayer(flacFile);
        } else {
            File wma = ConvertFlac.convertFlac(flacFile);
            if (Objects.isNull(wma)) throw new IllegalStateException("wma è null");
            Media media = new Media(wma.toPath().toUri().toString());
            AudioMediaPlayer amp = new AudioMediaPlayer(media);
            amp.setAudioFile(flacFile.toString());
            player = amp;
        }
        synchronized (this) {
            this.notify();
        }
    }


    public MediaPlayer getPlayer() {
        while (Objects.isNull(player)) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                return null;
            }
        }
        return player;
    }

    private boolean canReadFlac(File flacFile) {
        try {
            Player flacPlayer = new Player(null);
            flacPlayer.setTryingForTest(true);
            flacPlayer.decode(flacFile.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return !Objects.isNull(o) && o.getClass() == this.getClass() && ((Flac) o).ID == this.ID;
    }

}
