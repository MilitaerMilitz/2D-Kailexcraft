package com.github.kailex.api.util.files;

import com.github.kailex.api.util.LoggerUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class makes FileUtil.copyDirContent(...) monitorable.
 *
 * @author Alecander Ley
 * @version 1.0
 */
public class DirectoryContentCopier extends Thread implements IProgressRunnable{
    private final Logger LOGGER = LoggerUtil.getLogger(this.getClass());

    private final Path sourceDir;
    private final Path targetDir;
    private final long size;
    /**
     * Processing content (is used to calculate processed size correctly).
     */
    @Getter private final List<String> content;

    @Getter private boolean isReady;
    @Getter private boolean failure;

    /**
     * @throws IOException if an I/O error is thrown when accessing the starting file when calculating size.
     */
    public DirectoryContentCopier(Path sourceDir, Path targetDir) throws IOException {
        if (!sourceDir.toFile().exists()) throw new IllegalArgumentException(sourceDir + " does not exists.");
        if (!targetDir.toFile().exists()) throw new IllegalArgumentException(targetDir + " does not exists.");
        if (!sourceDir.toFile().isDirectory()) throw new IllegalArgumentException(sourceDir + " is not valid directory.");
        if (!targetDir.toFile().isDirectory()) throw new IllegalArgumentException(targetDir + " is not a valid directory.");
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.size = FileUtil.getDirectorySize(sourceDir, false);
        this.content = Files.list(sourceDir).map(path -> path.toFile().getName()).collect(Collectors.toList());
    }

    @Override
    public void run() {
        try {
            FileUtil.copyDirContent(sourceDir, targetDir);
            isReady = true;
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot copy directory content to target.", e);
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
        try{
            long sum = 0;

            for (String content : this.content){
                final Path path = targetDir.resolve(content);

                if (path.toFile().exists()) sum = FileUtil.getSize(path, true);
            }

            return sum;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot get size from moved files.");
            return -1;
        }
    }
}
