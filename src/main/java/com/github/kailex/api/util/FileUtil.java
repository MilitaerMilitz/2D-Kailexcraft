package com.github.kailex.api.util;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class offers basic method to operate with files.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class FileUtil {

    public static void downloadFile(Path destination, String httpUrl) throws IOException {
        if (!destination.toFile().exists()) Files.createFile(destination);

        final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(httpUrl).openStream());
        final FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile());
        final FileChannel fileChannel = fileOutputStream.getChannel();

        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    public static Path downloadTempFile(String httpUrl) throws IOException {
        final Path tempDest = Files.createTempFile("2D Kailexcraft", "downloadAssets");

        downloadFile(tempDest, httpUrl);
        return tempDest;
    }

    /**
     * Unzip zipFile to destinaten
     * @throws FileNotFoundException if file does not exist, is a directory rather than a regular file,
     * or for some other reason cannot be opened for reading.
     * @throws IOException if an I/O error has occurred.
     */
    public static void unzip(String dest, String zipFilePath) throws IOException {
        //Source Zip File
        String fileZip = zipFilePath.replace("%20", " ");

        //Output directory
        File destDir = new File(dest.replace("%20", " "));

        //Create Stream
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();

        //Write only .nbt files to directory
        byte[] buffer = new byte[1024];

        //Unzip all textures folder content
        while (zipEntry != null) {
            //Creates a new destination file where the bytes are copied in
            final File newFile = newFile(destDir, zipEntry);

            //Create Directory if it is one
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            }
            else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }


    /**
     * Creates new File at the destinationDir/zipEntry.
     * @throws IOException If entry is outside of the target directory.
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        //Creates destination File
        File destFile = new File(destinationDir, zipEntry.getName());

        //Checks if Entry is outside of the target dir
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Deletes directory with contents.
     * @throws NoSuchFileException if the file does not exist (optional specific exception).
     * @throws IOException if an I/O error occurs.
     */
    public static void deleteDirectory(File directoryToBeDeleted) throws IOException {
        if (!directoryToBeDeleted.isDirectory()) throw new IllegalArgumentException(directoryToBeDeleted.getName() + " is not a directory.");

        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        Files.delete(directoryToBeDeleted.toPath());
    }
}
