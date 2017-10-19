package ulg.programs.python_sorting;

import ulg.programs.ExternalPrograms;
import ulg.programs.utils.OsCheck;
import ulg.programs.utils.UnzipUtility;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Fabrizio
 * Date: 08/05/2017.
 */
public class PythonSorting {

    private static String path;

    public PythonSorting() {
        if (ExternalPrograms.OS == OsCheck.OSType.Windows) path = getMusicSortPath();
        else path = getMusicSortPath();
    }

    /**
     * @return il percorso della cartella "sorting"
     */
    public File getMusicSortURL() throws IOException, URISyntaxException {
        if (!checkSortingPath()) makeSortingFolder();
        return new File(path);
    }

    private String getMusicSortPath() {
        final String userPath = System.getProperty("user.home");
        System.out.println("\nuserPath = "+userPath);
        File musicPath = new File(userPath+"/"+"Musica");
        if (!musicPath.exists()) musicPath = new File(userPath+"/"+"Music");
        return musicPath.toString()+"/JavaMusicPlayer/sorting";
    }

    private void makeSortingFolder() throws URISyntaxException, IOException {
        InputStream stream = getClass().getResourceAsStream("/external_programs/sorting.zip");
        // System.out.println("resource = "+resource);
        // File file = new File(resource.toURI());
        // FileInputStream input = new FileInputStream(file);
        UnzipUtility unzipUtility = new UnzipUtility();
        unzipUtility.unzip(stream, path);
    }

    private boolean checkSortingPath() {
        File sortingPath = new File(path);
        if (!sortingPath.exists() && !sortingPath.mkdir()) throw new IllegalStateException("Unable to create directory");
        String[] files = sortingPath.list();
        if (Objects.isNull(files)) return false;
        List<String> checkNames = Arrays.asList("image", "modalities", "MusicSort.bat", "MusicSort.py");
        List<String> filesInFolder = Arrays.asList(files);
        return filesInFolder.containsAll(checkNames);
    }
}
