package ulg.programs.utils;


import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 *
 * Implemented by:
 * nclarkekb
 *
 * Source:
 * "https://github.com/netarchivesuite/heritrix3-wrapper/blob/master/src/main/java/org/netarchivesuite/heritrix3wrapper/unzip/UnzipUtility.java"
 */
public class UnzipUtility {

    /** Size of the buffer to read/write data. */
    private static final int BUFFER_SIZE = 8192;

    private byte[] tmpBuffer = new byte[BUFFER_SIZE];

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipStream input stream to unzip
     * @param destDirectory destination directory
     * @throws IOException if there's some error
     */
    public void unzip(InputStream zipStream, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(zipStream);
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            File destFile = new File(destDir, entry.getName());
            System.out.println("\nUnrar:  destFile = " + destFile.toString());
            long lastModified = entry.getTime();
            if (!entry.isDirectory()) {
                extractFile(zipIn, destFile);
                if (lastModified != -1) {
                    destFile.setLastModified(entry.getTime());
                }
            } else {
                destFile.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param destFile
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, File destFile) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
        int read = 0;
        while ((read = zipIn.read(tmpBuffer)) != -1) {
            bos.write(tmpBuffer, 0, read);
        }
        bos.close();
    }

}