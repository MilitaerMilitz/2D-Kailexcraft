package com.github.kailex.api.util.files;

import com.github.kailex.api.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * This class offers basic method to operate with files.
 *
 * @author Alexander Ley
 * @version 2.1
 */
public class FileUtil {
    public static final Logger LOGGER = LoggerUtil.getLogger("FileUtil");

    /**
     * Modifiable GSON object
     */
    public static final Gson GSON = new Gson();

    /**
     * Checks if httpUrl is correct.
     */
    public static boolean isUrlValid(String httpUrl){
        try {
            new URL(httpUrl);
            return true;
        }
        catch (MalformedURLException ex){
            return false;
        }
    }

    /**
     * If name cannot found behind link this method will generate a new unique name.
     * @param httpUrl direct downloadlink
     * @return Returns name of file behind direct downloadlink.
     * @throws IOException if an I/O error occurs or the temporary-file directory does not exist.
     */
    public static String getNameOfInternetFile(@NotNull String httpUrl) throws IOException {
        if (!isUrlValid(httpUrl)) throw new IllegalArgumentException(httpUrl + " is not valid.");

        final URL url = new URL(httpUrl);

        //open connection
        URLConnection con;
        try {
            con = url.openConnection();
        }
        catch (Exception ex){
            con = null;
        }

        //get and verify header field
        final String fieldValue;
        if (con == null || (fieldValue = con.getHeaderField("Content-Disposition")) == null
                || !fieldValue.contains("filename=")) {
            final Path tmp = Files.createTempFile("resourcepack", "");
            final String name = tmp.getFileName().toString();
            Files.delete(tmp);
            return name;
        }
        //parse file name from header field
        final List<String> arguments = List.of(fieldValue.split("; "));
        return arguments.stream().filter(s -> s.startsWith("filename=")).collect(Collectors.toList()).get(0)
                .replace("filename=", "")
                .replace("\"", "")
                .replace("%20", " ");
    }

    /**
     * @param httpUrl direct downloadlink.
     * @return Returns size of file behind downloadlink and -1 if size cannot be calculated.
     */
    public static long getSizeOfInternetFile(String httpUrl) {
        if (!isUrlValid(httpUrl)) throw new IllegalArgumentException(httpUrl + " is not valid.");

        final URL url;
        try {
            url = new URL(httpUrl);
        }
        catch (MalformedURLException e) {
            //Should not reachable.
            assert false;
            throw new IllegalArgumentException(httpUrl + " is not valid.");
        }

        URLConnection con = null;
        try {
            con = url.openConnection();
            if(con instanceof HttpURLConnection) {
                ((HttpURLConnection)con).setRequestMethod("HEAD");
            }

            con.getInputStream();
            return con.getContentLength();
        }
        catch (Exception e) {
            return -1;
        }
        finally {
            if(con instanceof HttpURLConnection) {
                ((HttpURLConnection)con).disconnect();
            }
        }
    }

    /**
     * Downloads a file from direct downloadlink into empty destination file.
     * @param destination empty file or not existent file (will be created)
     * @param httpUrl direct downloadlink
     * @throws FileNotFoundException if file does not exist but cannot be created or cannot be opened for any other reason
     * @throws IOException If an I/O error occurs.
     */
    public static void downloadFile(Path destination, String httpUrl) throws IOException {
        if (!isUrlValid(httpUrl)) throw new IllegalArgumentException(httpUrl + " is not valid.");

        final URL url = new URL(httpUrl);

        if (destination.toFile().isDirectory()) throw new IllegalArgumentException(destination + " is not file.");
        if (destination.toFile().exists() && !FileUtil.isPathEmpty(destination)) throw new IllegalArgumentException(destination + " is not empty.");
        if (!destination.toFile().exists()) Files.createFile(destination);

        final ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        final FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile());
        final FileChannel fileChannel = fileOutputStream.getChannel();

        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

        readableByteChannel.close();
        fileOutputStream.close();
        fileChannel.close();
    }

    /**
     * Downloads a file from direct downloadlink into tmp file.
     * @param httpUrl direct downloadlink
     * @return Returns created tmp file.
     * @throws FileNotFoundException if file does not exist but cannot be created or cannot be opened for any other reason
     * @throws IOException If an I/O error occurs.
     */
    public static Path downloadTempFile(String httpUrl) throws IOException {
        final Path tempDest = Files.createTempFile("2D Kailexcraft", "download");

        downloadFile(tempDest, httpUrl);
        return tempDest;
    }

    /**
     * Renames file or directory.
     * @return Returns true if and only if the renaming succeeded and false otherwise.
     */
    public static boolean rename(Path path, String name){
        final Path rePath = path.toFile().getAbsoluteFile().getParentFile().toPath().resolve(name);
        return path.toFile().renameTo(rePath.toFile());
    }

    /**
     * Move directory content to target directory. Deletes source directory.
     * @throws FileAlreadyExistsException – if directory/file could not otherwise be created because a directory/file of that name already exists (optional specific exception)
     * @throws IOException if an I/O error occurs when opening the directory.
     */
    public static void moveDirContent(Path sourceDir, Path targetDir) throws IOException {
        if (!sourceDir.toFile().exists()) throw new IllegalArgumentException(sourceDir + " does not exists.");
        if (!targetDir.toFile().exists()) throw new IllegalArgumentException(targetDir + " does not exists.");
        if (!sourceDir.toFile().isDirectory()) throw new IllegalArgumentException(sourceDir + " is not a directory.");
        if (!targetDir.toFile().isDirectory()) throw new IllegalArgumentException(targetDir + " is not a directory.");

        final List<Path> content = Files.list(sourceDir).collect(Collectors.toList());

        for (Path path : content){
            final String relative = path.toString().replace(sourceDir.toString(), "").substring(1);

            if (path.toFile().isDirectory()){
                final Path newPath = Files.createDirectory(targetDir.resolve(relative));
                moveDirContent(sourceDir.resolve(relative), newPath);
            }
            else{
                Files.move(path, targetDir.resolve(path.getFileName()));
            }
        }
        FileUtil.deleteDirectory(sourceDir);
    }

    /**
     * Copy directory content to another directory.
     * @throws FileAlreadyExistsException – if directory/file could not otherwise be created because a directory/file of that name already exists (optional specific exception)
     * @throws IOException if an I/O error occurs when opening the directory.
     */
    public static void copyDirContent(Path sourceDir, Path targetDir) throws IOException {
        if (!sourceDir.toFile().exists()) throw new IllegalArgumentException(sourceDir + " does not exists.");
        if (!targetDir.toFile().exists()) throw new IllegalArgumentException(targetDir + " does not exists.");
        if (!sourceDir.toFile().isDirectory()) throw new IllegalArgumentException(sourceDir + " is not a directory.");
        if (!targetDir.toFile().isDirectory()) throw new IllegalArgumentException(targetDir + " is not a directory.");

        final List<Path> content = Files.list(sourceDir).collect(Collectors.toList());

        for (Path path : content){
            final String relative = path.toString().replace(sourceDir.toString(), "").substring(1);

            if (path.toFile().isDirectory()){
                final Path newPath = Files.createDirectory(targetDir.resolve(relative));
                copyDirContent(sourceDir.resolve(relative), newPath);
            }
            else{
                Files.copy(path, targetDir.resolve(path.getFileName()));
            }
        }
    }

    /**
     * Checks if file is an archive (directories and not existing files are not an archive).
     */
    public static boolean isArchive(Path archive){
        if (archive.toFile().isDirectory() || !archive.toFile().exists()) return false;

        try{
            final ZipInputStream stream = new ZipInputStream(new FileInputStream(archive.toFile()));
            stream.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Returns list of first level archive content.
     */
    public static List<String> listFirstLevelArchiveContent(Path archive){
        if (!isArchive(archive)) throw new IllegalArgumentException(archive + " is not a valid archive file.");

        ZipFile zipFile = null;
        try {
            // open a zip file for reading
            zipFile = new ZipFile(archive.toFile());

            // get an enumeration of the ZIP file entries
            return zipFile.stream().map(zipEntry -> (zipEntry.isDirectory() ? "/" : "") + zipEntry.getName())
                    .filter(s -> !s.contains("/") || s.startsWith("/"))
                    .map(s -> (s.startsWith("/")) ? s.substring(0, s.indexOf("/", 1)) : s)
                    .distinct()
                    .collect(Collectors.toList());
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening zip file.", e);
            return Collections.emptyList();
        }
        finally {
            try {
                if (zipFile!=null) {
                    zipFile.close();
                }
            }
            catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error while closing zip file.", e);
            }
        }
    }

    /**
     * @return Returns list of all archive contents.
     */
    public static List<String> listArchiveContent(Path archive){
        if (!isArchive(archive)) throw new IllegalArgumentException(archive + " is not an archive file.");

        ZipFile zipFile = null;
        try {
            // open a zip file for reading
            zipFile = new ZipFile(archive.toFile());

            // get an enumeration of the ZIP file entries
            return zipFile.stream().map(ZipEntry::getName)
                    .collect(Collectors.toList());
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening zip file.", e);
            return Collections.emptyList();
        }
        finally {
            try {
                if (zipFile!=null) {
                    zipFile.close();
                }
            }
            catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error while closing zip file.", e);
            }
        }
    }

    /**
     * Extracts an archive into destination directory.
     * @throws FileAlreadyExistsException if directory does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if an I/O error has occurred or if entry is outside of target directory.
     */
    public static void extractArchive(Path archive, Path destinationDir) throws IOException{
        if (!destinationDir.toFile().exists()) throw new IllegalArgumentException(destinationDir + " does not exists.");
        if (!destinationDir.toFile().isDirectory()) throw new IllegalArgumentException(destinationDir + " is not a directory.");
        if (!isArchive(archive)) throw new IllegalArgumentException(archive + " is not a valid archive file.");

        //Create Stream
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(archive.toFile()));
        ZipEntry zipEntry = zis.getNextEntry();

        //Write only .nbt files to directory
        final byte[] buffer = new byte[1024];

        //Unzip all textures folder content
        while (zipEntry != null) {
            //Creates a new destination file where the bytes are copied in
            final File newFile = newFile(destinationDir.toFile(), zipEntry);

            //Create Directory if it is one
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            }
            else {
                // fix for Windows-created archives
                final File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                final FileOutputStream fos = new FileOutputStream(newFile);
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
     * @throws IOException If entry is outside of target directory.
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        //Creates destination File
        final File destFile = new File(destinationDir, zipEntry.getName());

        //Checks if Entry is outside of the target dir
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Calculates size of directory or file.
     * @param path directory or file
     * @param ignoreExceptions if selected files in directory where size cannot be calculated, will count as zero.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     */
    public static long getSize(Path path, boolean ignoreExceptions) throws IOException {
        return (path.toFile().isDirectory()) ? getDirectorySize(path, ignoreExceptions) : Files.size(path);
    }

    /**
     * Calculates only size of directory.
     * @param path only directories are allowed.
     * @param ignoreExceptions if selected files in directory where size cannot be calculated, will count as zero.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     */
    public static long getDirectorySize(Path path, boolean ignoreExceptions) throws IOException {
        if (!path.toFile().isDirectory()) throw new IllegalArgumentException(path + " is not a directory.");
        long size = 0;

        //Need of closing Stream walk
        try (Stream<Path> walk = Files.walk(path)) {
            size = walk.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        //ugly, can pretty it with an extract method
                        try {
                            return Files.size(p);
                        }
                        catch (IOException e) {
                            if (!ignoreExceptions) LOGGER.log(Level.WARNING, String.format("Failed to get size of %s%n%s", p, e));
                            return 0L;
                        }
                    }).sum();
        }
        catch (IOException e) {
            if (!ignoreExceptions) LOGGER.log(Level.SEVERE, String.format("IO errors %s", e));
        }

        return size;
    }

    /**
     * Checks if directory or file is empty (size is 0 bytes).
     * @param path directory or file.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     */
    public static boolean isPathEmpty(Path path) throws IOException {
        return getSize(path, false) == 0;
    }

    /**
     * This method uses the Path#resolve(relPath) method and returns its result and creates directory/file if it does not exist.
     * @param parent Path where resolve method is called.
     * @return Returns Path#resolve(relPath) result.
     * @throws IOException if an I/O error occurs or the parent directory does not exist.
     */
    public static Path validate(Path parent, String relPath, boolean isFile) throws IOException {
        final Path path = parent.resolve(relPath);
        if (!path.toFile().exists()){
            if (isFile) Files.createFile(path);
            else Files.createDirectory(path);
        }
        return path;
    }

    /**
     * Deletes directory with whole content recursively. If directory does not exist this method does nothing.
     * @throws IOException if an I/O error occurs.
     */
    public static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        if (!directoryToBeDeleted.toFile().exists()) return;
        if (!directoryToBeDeleted.toFile().isDirectory()) throw new IllegalArgumentException(directoryToBeDeleted.toFile().getName() + " is not a directory.");

        final File[] allContents = directoryToBeDeleted.toFile().listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) deleteDirectory(file.toPath());
                else Files.delete(file.toPath());
            }
        }
        Files.delete(directoryToBeDeleted);
    }

    /**
     * Saves obj to file using gson. Recreates file if it does already exist.
     * @throws IOException if an I/O error occurs by deleting old file.
     */
    public static <T> void saveToJson(Path path, T obj) throws IOException {
        if (path.toFile().exists() && !path.toFile().isFile()) throw new IllegalArgumentException(path + " is not a file.");
        if (path.toFile().exists()) Files.delete(path);

        final String json = GSON.toJson(obj);
        Files.writeString(path, json, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Saves obj to file using gson. Recreates file if it does already exist.
     * @throws IOException if an I/O error occurs by deleting old file.
     */
    public static <T> void saveToJson(Path path, T obj, TypeToken<T> token) throws IOException {
        if (path.toFile().exists() && !path.toFile().isFile()) throw new IllegalArgumentException(path + " is not a file.");
        if (path.toFile().exists()) Files.delete(path);

        final String json = GSON.toJson(obj, token.getType());
        Files.writeString(path, json, StandardOpenOption.CREATE_NEW);
    }

    /**
     * Loads object from file.
     * @return Returns in json stored object using gson.
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    public static <T> T loadFromJson(Path jsonPath, Class<T> clazz) throws IOException {
        if (!jsonPath.toFile().isFile()) throw new IllegalArgumentException(jsonPath + " is not a file.");
        if (!jsonPath.toFile().exists()) throw new IllegalArgumentException(jsonPath + " does not exists.");

        final String json = Files.readString(jsonPath);
        return GSON.fromJson(json, clazz);
    }

    /**
     * Loads object from file.
     * @return Returns in json stored object using gson.
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    public static <T> T loadFromJson(Path jsonPath, TypeToken<T> token) throws IOException {
        if (!jsonPath.toFile().isFile()) throw new IllegalArgumentException(jsonPath + " is not a file.");
        if (!jsonPath.toFile().exists()) throw new IllegalArgumentException(jsonPath + " does not exists.");

        final String json = Files.readString(jsonPath);
        return GSON.fromJson(json, token.getType());
    }
}
