package ulg.music;

import gui.utils.ProgressDialog;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import ulg.utils.UtilFunctions;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

import static gui.utils.ProgressDialog.TOTAL;
import static gui.utils.ProgressDialog.current;

/**
 * Created by: Fabrizio Fubelli
 * Date: 11/01/2017.
 */
public class MusicDictionary {
    private final static String EXTENSION = ".musicdict";
    private final File music_root;
    private final AtomicInteger number_of_files = new AtomicInteger(0);
    private final Map<String, String[]> music_map = Collections.synchronizedMap(new HashMap<>());
    private final File save_file;

    /**
     * Crea un nuovo MusicDictionary, tuttavia può comunicare con altri MusicDictionary tramite lettura di risorse
     * @param root la directory contenente i propri file musicali
     */
    public MusicDictionary(String root) {
        this.music_root = new File(root);
        this.save_file = new File(this.getSaveFolder(), this.getSaveName(root));
    }

    public File getSaveFile() {
        return this.save_file;
    }

    /**
     * @return la mappa dei file musicali nella directory scelta
     */
    public Map<String, String[]> getMap() {
        try {
            this.newMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return this.music_map;
    }

    /**
     * Crea un dizionario di tipo "file"=[list(string_attrs)]
     */
    @SuppressWarnings("unchecked")
    private void newMap() throws IOException, ClassNotFoundException {
        this.music_map.clear();
        if (needNewMap()) {
            if (!save_file.exists()) {
                System.out.println("DIZIONARIO NON PRESENTE");
                System.out.println("CREAZIONE NUOVO DIZIONARIO IN CORSO...");

                this.setTarget();

                this.recoursiveMapCreation(this.music_root);
                this.saveMap();
            } else {
                System.out.println("DIZIONARIO NON AGGIORNATO");
                if (!UtilFunctions.newDictionaryRequest(this.save_file, this.music_root)) {
                    System.out.println("AGGIORNAMENTO DIZIONARIO RIFIUTATO");
                    FileInputStream f = new FileInputStream(this.save_file);
                    ObjectInputStream o = new ObjectInputStream(f);
                    this.music_map.putAll((Map<String, String[]>) o.readObject());
                    current.set(ProgressDialog.TOTAL.get());
                } else {
                    System.out.println("CREAZIONE NUOVO DIZIONARIO IN CORSO...");
                    this.setTarget();
                    this.recoursiveMapCreation(this.music_root);
                    this.saveMap();
                }
            }
        } else {
            System.out.println("DIZIONARIO GIA' PRESENTE");
            FileInputStream f = new FileInputStream(this.save_file);
            ObjectInputStream o = new ObjectInputStream(f);
            this.music_map.putAll((Map<String, String[]>) o.readObject());
            current.set(ProgressDialog.TOTAL.get());
        }
    }

    private void setTarget() {
        this.number_of_files.set(0);
        this.getNumberOfFiles(this.music_root);
        System.out.println("numero di files nella cartella selezionata: "+this.number_of_files.get());
        TOTAL.set(this.number_of_files.get());
    }

    private void getNumberOfFiles(File file) {
        if (file.isFile()) this.number_of_files.getAndIncrement();
        else {
            final List<ForkJoinTask> tasks = new ArrayList<>();
            final File[] subfiles = file.listFiles();
            if (Objects.isNull(subfiles)) return;
            for (final File subfile : subfiles) tasks.add(ForkJoinTask.adapt(() -> getNumberOfFiles(subfile)));
            for (final ForkJoinTask t : ForkJoinTask.invokeAll(tasks)) {
                t.join();
                t.cancel(true);
            }
        }
    }

    /**
     * Fa una visita ricorsiva della cartella contenente la musica, creando un dizionario dei file musicali
     * @param file all'inizio è la cartella contenente la musica
     */
    private void recoursiveMapCreation(File file) {
        if (file.isFile()) {
            final AudioFile audioFile;
            try {
                audioFile = new AudioFileIO().readFile(file);
            } catch (CannotReadException | IOException | ReadOnlyFileException | InvalidAudioFrameException | TagException e) {
                current.getAndIncrement();
                return;
            }
            final AudioHeader audio = audioFile.getAudioHeader();
            final Tag tag = audioFile.getTag();
            String albumartist = tag.getFirst(FieldKey.ALBUM_ARTIST_SORT);
            String albumartistsort = tag.getFirst(FieldKey.ALBUM_ARTIST);
            String disconumber = tag.getFirst(FieldKey.DISC_NO);
            String disctotal = tag.getFirst(FieldKey.DISC_TOTAL);
            String album = tag.getFirst(FieldKey.ALBUM);
            String track = tag.getFirst(FieldKey.TRACK);
            String tracktotal = tag.getFirst(FieldKey.TRACK_TOTAL);
            String title = tag.getFirst(FieldKey.TITLE);
            String artist = tag.getFirst(FieldKey.ARTIST);
            String artistsort = tag.getFirst(FieldKey.ARTIST_SORT);
            String genre = tag.getFirst(FieldKey.GENRE);
            String duration = Double.toString(this.convertSecondsToMinutes(audio.getTrackLength()));
            String bitrate = audio.getBitRate();
            String filesize = Long.toString(file.length());
            String type = audio.getEncodingType();
            String[] metadata = {albumartist, albumartistsort, disconumber, disctotal, album, track, tracktotal, title,
                    artist, artistsort, genre, duration, bitrate, filesize, type};

            this.music_map.put(file.toString(), metadata);
            current.getAndIncrement();
            return;
        }
        final List<ForkJoinTask> tasks = new ArrayList<>();
        final File[] subfiles = file.listFiles();
        if (Objects.isNull(subfiles)) return;
        for (final File subfile : subfiles) tasks.add(ForkJoinTask.adapt(() -> recoursiveMapCreation(subfile)));
        for (final ForkJoinTask t : ForkJoinTask.invokeAll(tasks)) {
            t.join();
            t.cancel(true);
        }
    }

    /**
     * Chiamato da newMap, salva la mappa attuale
     */
    private void saveMap() {
        new Thread(() -> {
            try {
                FileOutputStream f = new FileOutputStream(this.save_file);
                ObjectOutputStream S = new ObjectOutputStream(f);
                S.writeObject(this.music_map);
                S.close();
                f.close();
                if (!this.save_file.setLastModified(this.music_root.lastModified())) {
                    throw new IllegalStateException("Impossibile aggiornare il file dizionario");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * @return il percorso della cartella "music_roots" interna al progetto
     */
    private File getSaveFolder() {
        final String userPath = System.getProperty("user.home");
        System.out.println("\nuserPath = "+userPath);
        File musicPath = new File(userPath+"/"+"Musica");
        if (!musicPath.exists()) musicPath = new File(userPath+"/"+"Music");
        if (!musicPath.exists()) musicPath.mkdir();
        File savePath = new File(musicPath.toString()+"/JavaMusicPlayer/playlists");
        if (!savePath.exists()) savePath.mkdirs();
        return savePath;
    }

    /**
     * Converte un percorso nel nome in cui verrebbe salvato il relativo file contenente il dizionario musicale
     * @param filepath il percorso della cartella contenente i file musicali
     * @return il nome del file contenente il dizionario della directory filepath
     */
    private String getSaveName(String filepath) {
        String newname = "";
        for (char c : filepath.toCharArray()) {
            if (c == '/' || c == '\\' || c == ':') newname+="_";
            else newname+=c;
        }
        newname+=EXTENSION;
        return newname;
    }

    /**
     * @return true, se serve creare o aggiornare la mappa musicale
     * @throws FileNotFoundException se
     */
    private boolean needNewMap() throws FileNotFoundException {
        if (!this.save_file.exists()) return true;
        return this.save_file.lastModified() != this.music_root.lastModified();
    }

    /**
     * converte un numero espresso in secondi, in un double espresso in minuti
     * @param seconds il numero da convertire
     * @return un numero di tipo mm.ss
     */
    private double convertSecondsToMinutes(int seconds) {
        int sec = seconds;
        int min = 0;
        while (sec > 60) {
            sec -= 60;
            min++;
        }
        return Double.valueOf(min + "." + sec);
    }
}
