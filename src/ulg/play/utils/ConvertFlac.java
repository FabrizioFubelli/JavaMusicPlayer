package ulg.play.utils;

import org.apache.commons.io.FileDeleteStrategy;
import org.jflac.apps.Decoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by: Fabrizio Fubelli
 * Date: 08/01/2017.
 */

public class ConvertFlac {

    private final static List<Track> Tracks = new ArrayList<>();
    private final static AtomicInteger HistoryTrack = new AtomicInteger(-1);
    private final static List<File> History = new ArrayList<>();
    private final static AtomicInteger TEMP_TRACK = new AtomicInteger(1);
    private final static AtomicInteger ACTUAL_TRACK = new AtomicInteger(1);
    private final static File OUTDIR = getTempFlacDir();

    ////////////////////////////////////////////////// PUBLIC METHODS //////////////////////////////////////////////////

    /**
     * @param flacFile The file to be converted
     * @return The converted file
     */
    static File convertFlac(File flacFile) {
        int lenH = History.size();
        if (! (lenH > 0 && Objects.equals(History.get(lenH-1), flacFile))) {
            History.add(flacFile);
            HistoryTrack.addAndGet(1);
        }
        if (Thread.currentThread().isInterrupted()) return null;
        FileNumber fn = convertFlacCall(flacFile);
        if (Objects.isNull(fn)) return null;
        if (fn.number == -1) return fn.file;
        Tracks.add(new Track(flacFile.getName(), fn.number));
        return fn.file;
    }

    /**
     * Deletes all of temporary files and resets all variables
     */
    public static void deleteTempFiles() {
        Tracks.clear();
        TEMP_TRACK.set(1);
        ACTUAL_TRACK.set(1);
        History.clear();
        try {
            File[] listFiles = OUTDIR.listFiles();
            if (Objects.isNull(listFiles)) return;
            for (File file : listFiles) file.deleteOnExit();
        } catch (Exception ignored) {}
    }

    /**
     * @return The previous track
     */
    public static File getPrevTrack() {
        if (HistoryTrack.get() < 0) return null;
        File flacFile = History.get(HistoryTrack.getAndDecrement());
        FileNumber fn = convertFlacCall(flacFile);
        return Objects.isNull(fn) ? null : fn.file;
    }

    /**
     * @return True, if there is an other previous track
     */
    public static boolean hasPrev() {
            return HistoryTrack.get() >= 0;
    }

    /**
     * @return The next Song
     */
    public static File getNextTrack() {
        if (HistoryTrack.get() >= History.size()) return null;
        File flacFile = History.get(HistoryTrack.getAndIncrement());
        FileNumber fn = convertFlacCall(flacFile);
        return Objects.isNull(fn) ? null : fn.file;
    }

    /**
     * @return True, if there is an other next track
     */
    public static boolean hasNext() {
        int n = HistoryTrack.get();
        return  -1 < n && n < History.size()-1;
    }







    /////////////////////////////////////////// PRIVATE METHODS AND CLASSES ////////////////////////////////////////////

    /**
     * Creates the directory where will be saved temporary files
     * @return The full path where will be saved temporary files
     */
    private static File getTempFlacDir() {
        Path currentRelativePath = Paths.get("");
        Path parentPath = currentRelativePath.toAbsolutePath();
        String s = parentPath.toString();
        File ret = new File(s+"/temp-wav");
        ret.mkdirs();
        ret.deleteOnExit();
        return ret;
    }

    /**
     * Checks if the Tracks list contains the serched track
     * @param name The name of the Song to search
     * @return The index of the finded Song, or -1
     */
    private static int tracksIndex(String name) {
        int i = 0;
        for (Track t : Tracks) {
            if (Objects.equals(t.name, name)) return i;
            i ++;
        }
        return -1;
    }

    /**
     * Updates the number of a Song
     * @param Old The old number of the Song
     * @param New The new number of the Song
     */
    /*private static void tracksChangeNumber(int Old, int New) {
        Tracks.forEach(t -> {if (t.number == Old) t.number = New;});
    }*/


    /**
     * Remove the track with selected number from the Tracks list
     * @param number The number of the track to remove
     */
    private static void removeTempTrack(int number) {
        for (int i = 0; i < Tracks.size(); i++) {
            Track t = Tracks.get(i);
            if (t.number == number) {
                Tracks.remove(i);
                return;
            }
        }
    }

    /**
     * Called by convertFlac method, needs to convert the flac file
     * @param flacFile The file that must be converted
     * @return  A FileNumber that contains the converted File and a number for the relative temp file
     */
    private static FileNumber convertFlacCall(File flacFile) {
        int indT = tracksIndex(flacFile.getName());
        if (indT > -1) {
            System.out.println("track already exists");
            Track findedTrack = Tracks.get(indT);
            String song = findedTrack.number + ".temp.wav";
            return new FileNumber(new File(OUTDIR, song), -1);
        }
        if (Thread.currentThread().isInterrupted()) return null;
        int newN = TEMP_TRACK.getAndAdd(1);
        ACTUAL_TRACK.set(newN);
        if (newN == 8) {    // MODIFICATO DA 10
            TEMP_TRACK.set(1);
        }
        String newName = newN + ".temp.wav";
        File jFlacOut = new File(OUTDIR, newName);
        if (jFlacOut.exists()) {
            long currentTime = System.currentTimeMillis();
            removeTempTrack(newN);
            while (jFlacOut.exists() && !FileDeleteStrategy.FORCE.deleteQuietly(jFlacOut)) {
                try {
                    if (System.currentTimeMillis() >= currentTime+2000) throw new IllegalStateException("Impossibile " +
                            "eliminare il file:\n"+jFlacOut.toString());
                    Thread.sleep(20);
                    FileDeleteStrategy.FORCE.delete(jFlacOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    break;
                }
            }
        }
        try {
            Decoder decoder = new Decoder();
            //System.out.println("\nConvertFlac -> calling Decoder.decode(). For file:"+"\nORIGINAL = "+flacFile.toString()+
             //       "\nCONVERTED = "+jFlacOut.toString());
            if (Thread.currentThread().isInterrupted()) return null;
            decoder.decode(flacFile.getAbsolutePath(), jFlacOut.getAbsolutePath());
            //System.out.println("\nConvertFlac -> Decoder.decode() done! For file:"+"\nORIGINAL = "+flacFile.toString()+
            //"\nCONVERTED = "+jFlacOut.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return new FileNumber(jFlacOut, newN);
    }



    /**
     * A Class to save some temporary Flac file details
     */
    private static class Track {
        private final String name;
        private int number;
        private Track(String name, int number) {
            this.name = name; this.number = number;
        }

        @Override
        public boolean equals(Object x) {
            if (!Objects.isNull(x) && x.getClass() == getClass()) {
                Track other = (Track) x;
                return Objects.equals(other.name, this.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.name);
        }
    }

    private static class FileNumber {
        private final File file;
        private final int number;
        private FileNumber(File file, int number) {
            this.file = file;
            this.number = number;
        }
    }
}
