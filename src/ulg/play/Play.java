package ulg.play;

import gui.Main;
import javafx.application.Platform;
import ulg.play.media.Media;
import ulg.play.media.MediaPlayer;
import ulg.play.players.ClassicMediaPlayer;
import ulg.play.utils.Flac;
import ulg.utils.RequestSem;
import ulg.utils.SongSem;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static gui.Main.*;

/**
 * Created by: Fabrizio Fubelli
 * Date: 08/01/2017.
 */
public class Play {
    public static final ExecutorService BACKGROUND_POOLS = Executors.newFixedThreadPool(3);
    public static final ExecutorService TASKS = Executors.newFixedThreadPool(1);

    private static volatile File[] sequenceFiles;
    private static volatile MediaPlayer[] sequencePlaying;
    private static final AtomicInteger requestNumber = new AtomicInteger(1);
    private static final AtomicInteger sequencePlayingIndex = new AtomicInteger();
    private static final Set<String> backgroundCalculating = Collections.synchronizedSet(new HashSet<>());
    private static final SongSem semSong = new SongSem();

    /**
     * Crea una lista di MediaPlayer, dei quali basterà avviare il primo per
     * riprodurre automaticamente in sequenza anche gli altri
     *
     * @param songs          la lista dei files musicali da riprodurre
     * @param firstSongIndex l'indice della prima canzone da riprodurre
     */
    public static synchronized void readyForNewPlaylist(List<Song> songs, int firstSongIndex) {
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        TASKS.submit(() -> {
            semSong.semWait(request);
            //System.out.println("\nPlay -> TASKS executing readyForNewPlaylist() -> notified");
            setNeedSong("");
            semRequest.semSignalAll();
            if (isPlaying()) stopFromThisThread();
            sequencePlayingIndex.set(firstSongIndex);
            //final File[] sequenceFilesTemp = new File[songs.size()];
            final MediaPlayer[] tempSequencePlaying = new MediaPlayer[songs.size()];
            if (!Objects.isNull(sequenceFiles) && songs.size() <= 50 && sequenceFiles.length <= 50) {
                sequenceFiles = new File[songs.size()];
                for (int n = 0; n < songs.size(); n++) {
                    File file = new File(songs.get(n).getFile());
                    int v = sequenceFilesContains(file);
                    if (v != -1) tempSequencePlaying[n] = sequencePlaying[v];
                    sequenceFiles[n] = file;
                }
            } else {
                sequenceFiles = new File[songs.size()];
                for (int n = 0; n < songs.size(); n++) {
                    sequenceFiles[n] = new File(songs.get(n).getFile());
                }
            }
            sequencePlaying = tempSequencePlaying;
            startBackgroundCalc();
            //System.out.println("Play -> TASKS executing readyForNewPlaylist() -> end");
            semSong.semSignal();
        });
    }

    private static void backgroundUpdate(final Integer index, final String file) {
        try {
            final MediaPlayer mp = prepareSong(sequenceFiles[index]);
            mp.setOnEndOfMedia(() -> {
                try {
                    next();
                } catch (Exception ignored) {
                    // skip
                }
            });
            synchronized (backgroundCalculating) {
                backgroundCalculating.remove(file);
                backgroundCalculating.notifyAll();
            }
            sequencePlaying[index] = mp;
        } catch (Exception e) {
            e.printStackTrace();
            if (backgroundCalculating.contains(file)) {
                synchronized (backgroundCalculating) {
                    backgroundCalculating.remove(file);
                    backgroundCalculating.notifyAll();
                }
            }
        }
    }

    private static void startBackgroundCalc() {
        final int INDEX = sequencePlayingIndex.get();
        int index = INDEX;
        for (int i = 0; i < 3; i++) {
            try {
                if (index < 0 || index >= sequenceFiles.length || !Objects.isNull(sequencePlaying[index]))
                    throw new Exception();
            } catch (Exception e) {
                if (index == INDEX) index = INDEX-1;
                else index = INDEX+1;
                continue;
            }
            String file = sequenceFiles[index].toString();
            if (!backgroundCalculating.add(file)) {
                if (index == INDEX) index = INDEX-1;
                else index = INDEX+1;
                continue;
            }
            final int j = index;
            Runnable r = () -> {
                try {
                    backgroundUpdate(j, file);
                } catch (ArrayIndexOutOfBoundsException e) {
                    calculatingSongs.remove(file);
                }
            };
            calculatingSongs.add(file);
            BACKGROUND_POOLS.execute(r);
            if (index == INDEX) index = INDEX-1;
            else index = INDEX+1;
        }
    }

    private static int sequenceFilesContains(File file) {
        if (Objects.isNull(sequenceFiles)) return -1;
        int i = 0;
        for (File f : sequenceFiles) {
            if (Objects.equals(file, f)) return i;
            i++;
        }
        return -1;
    }

    /*  NON ANCORA IN USO
    public static double getCurrentTimeSeconds() {
        try {
            return sequencePlaying[sequencePlayingIndex.get()].getCurrentTime().toSeconds();
        } catch (Exception e) {
            return 0;
        }
    }*/

    private static volatile boolean isPlaying = false;

    public static synchronized boolean isPlaying() {
        return isPlaying;
    }

    private static volatile boolean isPaused = false;

    public static synchronized boolean isPaused() {
        return isPaused;
    }

    private static volatile boolean isStopped = false;

    public static synchronized boolean isStopped() {
        return isStopped;
    }

    private static boolean canPlay() {
        return sequencePlayingIndex.get() < sequencePlaying.length && sequencePlaying.length > 0;
    }

    public static synchronized void play() {
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        TASKS.submit(() -> {
            semSong.semWait(request);
            final MediaPlayer p;
            try {
                System.out.println("\nPlay -> TASKS executing play() -> notified");
                isPlaying = true;
                isPaused = false;
                isStopped = false;
                //PLAY_PAUSE.mouseClick();

                updateNeedSong();
                semRequest.semSignalAll();
                if (requestNumber.get() > request) {
                    semSong.semSignal();
                    return;
                }
                updateMedia();
                if (!canPlay()) throw new IllegalStateException("Nessuna traccia da eseguire");
                p = sequencePlaying[sequencePlayingIndex.get()];
                if (Objects.isNull(p) || requestNumber.get() > request) {
                    System.out.println("Play -> TASKS executing play() -> p is null or request is too old");
                    semSong.semSignal();
                    return;
                }
                System.out.println("Play -> TASKS executing play() -> p.play() calling...");
                p._play();
                System.out.println("Play -> TASKS executing play() -> p.play() CALLED!");
                final String forTitle = p.getFile();
                Platform.runLater(() -> primaryStage.setTitle(NAME + " (" + forTitle + ")"));
                updateButtons();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Play -> TASKS executing play() -> end");
            semSong.semSignal();
        });
    }

    public static synchronized void pause() {
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        TASKS.submit(() -> {
            semSong.semWait(request);
            updateNeedSong();
            semRequest.semSignalAll();
            try {
                System.out.println("\nPlay -> TASKS executing pause() -> notified");
                isPlaying = false;
                isPaused = true;
                isStopped = false;
                sequencePlaying[sequencePlayingIndex.get()]._pause();
                updateButtons();
            } catch (NullPointerException ignored) {
            }
            semSong.semSignal();
        });
    }

    public static synchronized void stop() {
        System.out.println("\nPlay -> stop() -> called");
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        //System.out.println("\nPlay -> stop() -> called (flag 1)");
        TASKS.submit(() -> {
            //System.out.println("\nPlay -> TASKS executing stop() -> called");
            semSong.semWait(request);
            updateNeedSong();
            semRequest.semSignalAll();
            try {
                //System.out.println("\nPlay -> TASKS executing stop() -> notified");
                isPlaying = false;
                isPaused = false;
                isStopped = true;
                Platform.runLater(() -> primaryStage.setTitle(NAME));
                sequencePlaying[sequencePlayingIndex.get()]._stop();
                updateButtons();
            } catch (Exception ignored) {
                // skip
            }
            semSong.semSignal();
        });
    }

    private static void stopFromThisThread() {
        //System.out.println("\nPlay -> TASKS executing stop() -> notified");
        isPlaying = false;
        isPaused = false;
        isStopped = true;
        Platform.runLater(() -> primaryStage.setTitle(NAME));
        try {
            sequencePlaying[sequencePlayingIndex.get()]._stop();
        } catch (NullPointerException ignored) {
            // skip
        }
        updateButtons();
    }

    private static boolean hasPrevious() {
        try {
            return sequencePlayingIndex.get() > 0 || isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void previous() {
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        TASKS.submit(() -> {
            semSong.semWait(request);
            //semRequest.semSignalAll();
            MediaPlayer actual = sequencePlaying[sequencePlayingIndex.get()];
            double actual_seconds = actual.getCurrentTime().toSeconds();
            MediaPlayer.Status status = actual.getStatus();
            actual._stop();
            if (status == MediaPlayer.Status.PLAYING) {
                if (actual_seconds > 5 || sequencePlayingIndex.get() == 0) {
                    if (requestNumber.get() > request) {
                        semSong.semSignal();
                        return;
                    }
                    if (actual.getType() == MediaPlayer.PlayerType.NORMAL) actual._stop();
                    actual._play();
                } else {
                    if (!hasPrevious()) throw new IllegalStateException("Nessuna traccia precedente presente");
                    sequencePlayingIndex.decrementAndGet();

                    updateNeedSong();
                    semRequest.semSignalAll();

                    if (requestNumber.get() > request) {
                        semSong.semSignal();
                        return;
                    }

                    updateMedia();
                    final MediaPlayer p = sequencePlaying[sequencePlayingIndex.get()];
                    if (Objects.isNull(p)) {
                        semSong.semSignal();
                        return;
                    }

                    Platform.runLater(() -> primaryStage.setTitle(NAME+" ("+p.getFile()+")"));
                    if (actual.getType() == MediaPlayer.PlayerType.NORMAL) actual._stop();
                    p._play();
                }
            } else {
                if (!(hasPrevious() || isPlaying()))
                    throw new IllegalStateException("Nessuna traccia precedente presente");
                if (sequencePlayingIndex.get() > 0) sequencePlayingIndex.getAndDecrement();

                updateNeedSong();
                semRequest.semSignalAll();

                if (requestNumber.get() > request) {
                    semSong.semSignal();
                    return;
                }

                updateMedia();
                final MediaPlayer p = sequencePlaying[sequencePlayingIndex.get()];
                if (Objects.isNull(p) || requestNumber.get() > request) {
                    semSong.semSignal();
                    return;
                }
                Platform.runLater(() -> primaryStage.setTitle(NAME+" ("+p.getFile()+")"));
            }
            updateButtons();
            semSong.semSignal();
        });
    }

    private static boolean hasNext() {
        try {
            return sequencePlayingIndex.get() < sequencePlaying.length - 1 && sequencePlaying.length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void next() {
        final Integer request = requestNumber.incrementAndGet();
        semSong.newRequest(request);
        TASKS.submit(() -> {
            semSong.semWait(request);
            try {
                if (!hasNext()) throw new IllegalStateException("Nessuna traccia successiva presente");
                //semRequest.semSignalAll();
                MediaPlayer actual = sequencePlaying[sequencePlayingIndex.getAndIncrement()];

                updateNeedSong();
                semRequest.semSignalAll();

                if (requestNumber.get() > request) {
                    semSong.semSignal();
                    return;
                }

                updateMedia();
                MediaPlayer.Status status = actual.getStatus();
                actual._stop();
                final MediaPlayer p = sequencePlaying[sequencePlayingIndex.get()];
                if (Objects.isNull(p) || requestNumber.get() > request) {
                    semSong.semSignal();
                    return;
                }
                Platform.runLater(() -> primaryStage.setTitle(NAME+" ("+p.getFile()+")"));
                if (status == MediaPlayer.Status.PLAYING) {
                    p._play();
                }
            } catch (Exception ignored) {
                // skip
            }
            updateButtons();
            semSong.semSignal();
        });
    }

    public static void off() {
        TASKS.shutdownNow();
        stopFromThisThread();
    }

    private static void updateMedia() {
        final int index = sequencePlayingIndex.get();
        final File file;
        file = sequenceFiles[index];
        if (!Objects.isNull(sequencePlaying[index])) return;
        disableAllButtons(true);
        if (backgroundCalculating.contains(file.toString())) {
            //System.out.println("\nPlay -> updateMedia() -> waiting for backgroundCalc");
            while (backgroundCalculating.contains(file.toString())) {
                synchronized (backgroundCalculating) {
                    try {
                        backgroundCalculating.wait();
                        System.out.println("updateMedia 7");
                        System.out.println("backgroundCalculating:");
                        System.out.println(backgroundCalculating.toString());
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
            //System.out.println("Play -> updateMedia() -> return after backgroundCalc terminated!\n");
            disableAllButtons(false);
            return;
        }
        try {
            final MediaPlayer mp = prepareSong(file);
            mp.setOnEndOfMedia(() -> {
                try {
                    next();
                } catch (Exception ignored) {
                    // skip
                }
            });
            sequencePlaying[index] = mp;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // skip
        }
        disableAllButtons(false);
    }

    private static void updateButtons() {
        System.out.println("updateButtons CALLED");
        Main.PREVIOUS.setButtonDisabled(!(hasPrevious() || isPlaying() || isStopped()));
        Main.NEXT.setButtonDisabled(!hasNext());
        Main.STOP.setButtonDisabled(!(canPlay() && !isStopped()));
        PLAY_PAUSE.setPlayButton(!isPlaying());
        System.out.println("updateButtons END");
    }

    private static void disableAllButtons(boolean disable) {
        PLAY_PAUSE.setButtonDisabled(disable);
        Main.PREVIOUS.setButtonDisabled(disable);
        Main.NEXT.setButtonDisabled(disable);
        Main.STOP.setButtonDisabled(disable);

    }

    private static synchronized void updateNeedSong() {
        final int index = sequencePlayingIndex.get();
        if (sequenceFiles.length < index) return;
        setNeedSong(sequenceFiles[index].toString());
    }

    private static synchronized void setNeedSong(String song) {
        needSong = song;
    }

    private static synchronized String getNeedSong() {
        return needSong;
    }

    private static final RequestSem semRequest = new RequestSem();
    private static final Set<String> calculatingSongs = Collections.synchronizedSet(new HashSet<>());
    private static volatile String needSong;

    /**
     * Prepara l'oggetto MediaPlayer che verrà riprodotto. Può richiedere parecchio tempo computazionale
     * @param song il file contenente la canzone
     * @return un MediaPlayer in grado di riprodurre musica
     */
    private static MediaPlayer prepareSong(final File song) {
        String s = song.toString();
        int len = s.length();
        if (Objects.equals(s.substring(len - 5), ".flac")) {

            // In questo caso, bisogna ridurre al minimo il tempo di risposta, in caso l'utente scelga
            // un altro brano durante la lettura del Flac

            // Utilizzerò un semaforo Weak, con cui sbloccherò tutti i processi in attesa della computazione del Flac
            // nel caso in cui verrà richiesto un altro brano durante la lettura di questo flac

            final MediaPlayerContainer player = new MediaPlayerContainer();
            semRequest.semWait();   // segnala di aver cominciato questo processo
            final Thread calc = new Thread(() -> {
                try {
                    Flac flac = new Flac(song);
                    player.set(flac.getPlayer());
                    synchronized (semRequest) {
                        semRequest.notifyAll();
                    }
                } catch (IllegalStateException e) {
                    // skip
                }
            });
            calc.start();
            while (player.isEmpty() && (calculatingSongs.contains(getNeedSong()) || !semRequest.canPass()))  {
                synchronized (semRequest) {
                    try {
                        semRequest.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (calculatingSongs.contains(needSong) && semRequest.canPass()) semRequest.semWait();
                }
            }
            // Se canPass è true, vuoldire che una nuova richiesta da parte dell'utente ha risvegliato tutti i thread in
            // attesa del semaforo "semRequest"
            calc.interrupt();
            if (semRequest.canPass() && !calculatingSongs.contains(needSong)) {
                calculatingSongs.remove(s);
                //throw new IllegalStateException("Other request in queue");
            }
            semRequest.semSignal(); // segnala di aver completato questo processo
            calculatingSongs.remove(s);
            return player.get();
        } else {
            String s3 = s.substring(len - 4);
            System.out.println("prepareSong OK, type: "+s3);
            if (Objects.equals(s3, ".mp3") || Objects.equals(s3, ".wav")) {
                System.out.println("prepareSong OK 2");
                calculatingSongs.remove(s);
                System.out.println("prepareSong OK 3 (return)");
                return playSimply(song);
            }
        }
        throw new IllegalArgumentException("Il file "+song+" non può essere letto!\nProvare ad aggiungere " +
                "il suo tipo di estensione nel metodo Play.prepareSong(File song).");
    }

    private static MediaPlayer playSimply(File simplyFile) {
        try {
            System.out.println("playSimply OK");
            ClassicMediaPlayer cmp = null;
            System.out.println("playSimply OK 2");
            while(Objects.isNull(cmp)) {
                try {
                    System.out.println("playSimply OK 3");
                    //amp = new AudioMediaPlayer(new Media(simplyFile.toPath().toUri().toString()));
                    cmp = new ClassicMediaPlayer(simplyFile);
                    System.out.println("playSimply OK 4, file:");
                    System.out.println(simplyFile.toPath().toUri().toString());
                } catch (Exception e) {
                    //e.printStackTrace();
                    throw e;
                    // skip
                }
                Thread.sleep(20);
            }
            //System.out.println("playSimply OK 5");
            //cmp.setAudioFile(simplyFile.toString());
            System.out.println("playSimply OK 5 (return)");
            return cmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static class MediaPlayerContainer {
        private volatile MediaPlayer mediaPlayer;
        private synchronized void set(MediaPlayer mp) { this.mediaPlayer = mp; }
        private synchronized MediaPlayer get() { return this.mediaPlayer; }
        private synchronized boolean isEmpty() { return Objects.isNull(this.mediaPlayer); }
    }
}
