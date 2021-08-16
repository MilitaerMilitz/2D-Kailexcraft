package com.github.kailex.api.util.files;

import com.github.kailex.api.util.LoggerUtil;
import lombok.Getter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class makes FileUtil.downloadFile(...) monitorable.
 *
 * @author Alecander Ley
 * @version 1.0
 */
public class FileDownloader extends Thread implements IProgressRunnable{
    private final Logger LOGGER = LoggerUtil.getLogger(this.getClass());

    private final String httpUrl;
    @Getter private final Path destination;
    private final long size;

    @Getter private boolean isReady;
    @Getter private boolean failure;

    /**
     * @throws IOException if an I/O error is thrown when accessing the starting file when calculating destination size.
     */
    public FileDownloader(String httpUrl, Path destination, long size) throws IOException {
        try {
            new URL(httpUrl);
        }
        catch (MalformedURLException ex){
            LOGGER.log(Level.WARNING, httpUrl + " is not valid.", ex);
            throw new IllegalArgumentException(httpUrl + " is not valid.");
        }
        if (destination.toFile().isDirectory()) throw new IllegalArgumentException(destination + " is not file.");
        if (destination.toFile().exists() && !FileUtil.isPathEmpty(destination)) throw new IllegalArgumentException(destination + " is not empty.");

        this.httpUrl = httpUrl;
        this.destination = destination;
        this.size = size;
    }

    public FileDownloader(String httpUrl, long size) throws IOException {
        this(httpUrl, Files.createTempFile("2D Kailexcraft", "download"), size);
    }

    @Override
    public void run() {
        try {
            FileUtil.downloadFile(destination, httpUrl);
            isReady = true;
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot download file.");
            isReady = true;
            failure = true;
        }
    }

    @Override
    public long processDataSize() {
        return size;
    }

    @Override
    public long getProcessedSize(){
        try {
            return Files.size(destination);
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot get size from downloaded file.");
            return -1;
        }
    }
}
