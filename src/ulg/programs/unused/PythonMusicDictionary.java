package ulg.programs.unused;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Crea un dizionario di elementi musicali (.mp3 e .flac)
 */
public class PythonMusicDictionary {
    private final String LastModifiedDateDF;
    private final String DetailedFilesURL;
    private final Path SortedMusic;
    private final Scanner DetailedFiles;
    private final List<String> DF_Lines = new ArrayList<>();
    private final Map<String, List<String>> Dictionary = new HashMap<>();

    /**
     * Crea un PythonMusicDictionary
     * @param Sorted_Music La directory della cartella Sorted_Music
     * @throws FileNotFoundException Se il file "Sorted_Music/MUSIC_SORT/DetailedFiles.np" non è presente
     */
    public PythonMusicDictionary(Path Sorted_Music) throws FileNotFoundException {
        this.SortedMusic = Sorted_Music;
        this.DetailedFilesURL = this.SortedMusic.toString()+"/MUSIC_SORT/DetailedFiles.np";
        this.DetailedFiles = new Scanner(new FileReader(this.DetailedFilesURL));
        this.LastModifiedDateDF = this.DetailedFiles.nextLine();
        this.getDetailedFiles();
        this.getMap();
    }

    /**
     * @return Una lista immodificabile di tutti i files presenti nel dizionario con i relativi attributi
     */
    public List<String> getDetailedFiles() throws FileNotFoundException {
        if (this.DF_Lines.isEmpty()) {
            while (this.DetailedFiles.hasNextLine()) {
                String line = this.DetailedFiles.nextLine();
                if (line.length() > 5 && Objects.equals(line.substring(0,5), "FILE=")) {
                    this.DF_Lines.add(line);
                }
            }
            this.DetailedFiles.close();
            Collections.sort(this.DF_Lines);
        }
        return Collections.unmodifiableList(this.DF_Lines);
    }

    /**
     * @return il Dizionario immodificabile contenente tutti i file musicali
     */
    public Map<String, List<String>> getMap() {
        if (this.Dictionary.isEmpty()) {
            this.DF_Lines.forEach(line -> this.CreateDictionaryLineAttr(line.trim()));
        }
        return Collections.unmodifiableMap(this.Dictionary);
    }

    public String getDetailedFilesURL() {
        return this.DetailedFilesURL;
    }

    public String getSortedMusicPath() {
        return this.SortedMusic.toString();
    }

    public String getLastModifiedDate() {
        return this.LastModifiedDateDF;
    }

    public List<String> getAttributes() { return Collections.unmodifiableList(this.Attr);}

    private void CreateDictionaryLineAttr(String line) {
        this.CreateDictionaryLineAttr(line, "",  1, 5);
    }

    @SuppressWarnings("unchecked")
    private void CreateDictionaryLineAttr(String line, String key, int ind, int u) {
        try {
            String next_attr = this.Attr.get(ind);
            int len_next_attr = next_attr.length();
            int new_u = u;
            while (!Objects.equals(line.substring(new_u, new_u+len_next_attr), next_attr)){
                new_u ++;
            }
            if (ind == 1) {
                key = line.substring(u,new_u - 3).trim();
                this.Dictionary.put(key, new ArrayList<>());
            } else {
                this.Dictionary.get(key).add(line.substring(u, new_u-3).trim());
            }
            this.CreateDictionaryLineAttr(line, key, ind+1, new_u+len_next_attr);
        } catch (Exception e) {  // Arrivati all'ultimo attributo
            this.Dictionary.get(key).add(line.substring(u).trim());
        }
    }

    public static final List<String> Attr = Arrays.asList("FILE=", "ALBUMARTIST=", "DISC_NUMBER=", "DISC_TOTAL=", "ALBUM=",
            "TRACK=", "TITLE=", "ARTIST=", "DURATION=", "BITRATE=", "FILESIZE=", "MODIFIED=");
}
