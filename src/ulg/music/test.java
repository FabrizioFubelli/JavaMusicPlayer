package ulg.music;

import java.io.File;

/**
 * Created by: Fabrizio Fubelli
 * Date: 12/01/2017.
 */
public class test {
    public static void main(String[] args) {
        test();
    }
    private static void test() {
        File root = new File("E:\\Root\\Sorted_Music");
        MusicDictionary md = new MusicDictionary(root.toString());
        System.out.println(md.getSaveFile().lastModified() == root.lastModified());
    }
}
