package com.github.kailex.api.util.files;

import com.github.kailex.api.util.LoggerUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class makes FileUtil.extractArchive(...) monitorable.
 *
 * @author Alecander Ley
 * @version 1.0
 */
public class FileExtractor extends Thread implements IProgressRunnable{

    private final Logger LOGGER = LoggerUtil.getLogger(this.getClass());

    private final Path archive;
    @Getter private final Path destinationDir;

    @Getter private final List<String> firstArchiveContent;
    private final long size;

    @Getter private boolean isReady;
    @Getter private boolean failure;

    /**
     * @throws IOException if an I/O error occurs when calculating size.
     */
    public FileExtractor(Path archive, Path destinationDir) throws IOException {
        if (!destinationDir.toFile().exists()) throw new IllegalArgumentException(destinationDir + " does not exists.");
        if (!destinationDir.toFile().isDirectory()) throw new IllegalArgumentException(destinationDir + " is not a directory.");
        if (!FileUtil.isArchive(archive)) throw new IllegalArgumentException(archive + " is not a valid archive file.");

        this.archive = archive;
        this.destinationDir = destinationDir;
        this.firstArchiveContent = FileUtil.listFirstLevelArchiveContent(archive);
        this.size = Files.size(archive);
    }

    @Override
    public void run() {
        try {
            FileUtil.extractArchive(archive, destinationDir);
            isReady = true;
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot unzip file.");
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
            long sum = 0;

            for (String content : firstArchiveContent){
                final Path path = destinationDir.resolve((content.startsWith("/")) ? content.substring(1) : content);

                if (path.toFile().exists()) sum = FileUtil.getSize(path, true);
            }

            return sum;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot get size from progress file.");
            return -1;
        }
    }
}
