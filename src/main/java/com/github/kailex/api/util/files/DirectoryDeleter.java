package com.github.kailex.api.util.files;

import com.github.kailex.api.util.LoggerUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class makes FileUtil.deleteDirectory(...) monitorable.
 *
 * @author Alecander Ley
 * @version 1.0
 */
public class DirectoryDeleter extends Thread implements IProgressRunnable{
    private final Logger LOGGER = LoggerUtil.getLogger(this.getClass());

    private final Path targetDir;
    private final long size;

    @Getter private boolean isReady;
    @Getter private boolean failure;

    /**
     * @throws IOException if an I/O error is thrown when accessing the starting file when calculating size.
     */
    public DirectoryDeleter(Path targetDir) throws IOException {
        if (!targetDir.toFile().isDirectory()) throw new IllegalArgumentException(targetDir + " is not a valid directory.");
        this.targetDir = targetDir;
        this.size = FileUtil.getDirectorySize(targetDir, false);
    }

    @Override
    public void run() {
        try {
            FileUtil.deleteDirectory(targetDir);
            isReady = true;
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot delete directory content.", e);
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
            return size - FileUtil.getDirectorySize(targetDir, true);
        }
        catch (IllegalArgumentException il){
            return 0;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot get size from deleted files.");
            return -1;
        }
    }
}
