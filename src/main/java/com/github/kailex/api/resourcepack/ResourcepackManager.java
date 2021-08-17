package com.github.kailex.api.resourcepack;

import com.github.kailex.api.util.LoggerUtil;
import com.github.kailex.api.util.Tickable;
import com.github.kailex.api.util.Tuple;
import com.github.kailex.api.util.files.*;
import com.github.kailex.fxWindow.KailexApp;
import com.github.kailex.fxWindow.LoadingScreenController;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles all basic resourcepack management features and offers methods to load resourcepacks.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class ResourcepackManager {
    public static final Logger LOGGER = LoggerUtil.getLogger("ResourcepackManager");

    /**
     * Default assets data
     */
    public static final String ASSETS_URL = "https://github.com/InventivetalentDev/minecraft-assets/zipball/refs/heads/1.17.1";
    public static final long ZIP_SIZE = 315_908_094;
    public static final long UNZIPPED_SIZE = 408_432_161;

    private static @Nullable Tickable actionMonitor;

    /**
     * This method install default pack (on separate thread) if it does not exist or if it needs an update.
     * To install default pack will be downloaded into resourcepack folder, and it will be renamed to "default_pack.zip".
     * @return Returns latch which marks if downloading is ready.
     * @throws IOException if an I/O error occurs or the parent directory does not exist or if method cannot wait for runnable.
     */
    public static CountDownLatch installDefaultPack() throws IOException {
        final Path resourcePackPath = validate("resourcepack", false);
        final Path defaultPack = resourcePackPath.resolve("default_pack");
        final CountDownLatch readyLatch = new CountDownLatch(1);

        if (defaultPack.toFile().exists() && FileUtil.getSize(defaultPack, false) == UNZIPPED_SIZE) {
            readyLatch.countDown();
            return readyLatch;
        }

        FileUtil.deleteDirectory(defaultPack);

        Thread action = new Thread(() -> {
            try {
                //Download Default Pack
                Tuple<FileDownloader, CountDownLatch> retDown = downloadPack(ASSETS_URL, ZIP_SIZE);

                final FileDownloader downloader = retDown.getKey();
                CountDownLatch internLatch = retDown.getValue();

                try {
                    internLatch.await();
                    assert !downloader.isFailure();
                }
                catch (InterruptedException e) {
                    throw new IOException("Cannot download pack.");
                }

                //Finishing up.
                showProgress("Renaming files", -1);
                if (!FileUtil.rename(downloader.getDestination(), "default_pack.zip")){
                    throw new IOException("Cannot rename pack.");
                }
            }
            catch (IOException e){
                throw new RuntimeException(e.getMessage(), e);
            }
            finally {
                readyLatch.countDown();
            }
        });
        action.start();

        return readyLatch;
    }

    /**
     * This method downloads a resourcepack into resourcepack folder (on separate thread).
     * @param size if null size will be calculated with FileUtil.getNameOfInternetFile(...)
     * @return Returns tuple containing information about download progress. First, a downloader and second a latch marking if downloading is ready.
     * @throws IOException if an I/O error occurs or the parent directory does not exist or if method cannot get information about downloading file.
     */
    public static Tuple<FileDownloader, CountDownLatch> downloadPack(String httpUrl, @Nullable Long size) throws IOException {
        if (!FileUtil.isUrlValid(httpUrl)) throw new IllegalArgumentException(httpUrl + " is not valid.");

        final Path resourcePackPath = validate("resourcepack", false);

        final FileDownloader downloader = new FileDownloader(httpUrl,
                FileUtil.validate(resourcePackPath, FileUtil.getNameOfInternetFile(httpUrl), true),
                (size == null) ? FileUtil.getSizeOfInternetFile(httpUrl) : size
        );
        downloader.start();

        return new Tuple<>(downloader, monitorAction(downloader, "Downloading assets"));
    }

    /**
     * Apply default pack, by installing it if needed, extracting it and moving it into target resource folder.
     * @param force if true method load pack in every case and if false pack will not be loaded if it is already loaded.
     * @return Returns latch which marks if applying is ready.
     * @throws IOException if an I/O error occurs or the parent directory does not exist or if method cannot wait for runnable.
     */
    public static CountDownLatch applyDefaultPack(boolean force) throws IOException {
        final Path resourcePath = validate("resource", false);
        final Path resourcePackPath = validate("resourcepack", false);
        final Path pack = resourcePackPath.resolve("default_pack.zip");
        final CountDownLatch readyLatch = new CountDownLatch(1);

        clearAssets();

        Thread action = new Thread(() -> {
            try {
                CountDownLatch internLatch;

                if (!pack.toFile().exists() || Files.size(pack) != ZIP_SIZE){
                    Files.deleteIfExists(pack);

                    internLatch = installDefaultPack();

                    try {
                        internLatch.await();
                    }
                    catch (InterruptedException e) {
                        throw new IOException("Cannot install default pack.");
                    }
                }

                if (FileUtil.getDirectorySize(resourcePath, false) == UNZIPPED_SIZE
                        && !FileUtil.isPathEmpty(resourcePath)
                        && !force
                        && KailexApp.GAME_SETTINGS.getActiveResourcepack().equals(pack.toFile().getName())) {
                    LOGGER.log(Level.INFO, "Resourcepack is already selected.");
                    readyLatch.countDown();
                    return;
                }

                //Delete old assets
                if (!FileUtil.isPathEmpty(resourcePath)) {
                    startAndWait(new DirectoryDeleter(resourcePath), "Deleting old files");
                    Files.createDirectory(resourcePath);
                }

                //Extract files
                final FileExtractor extractor = new FileExtractor(pack, resourcePackPath);
                startAndWait(extractor, "Extracting assets");

                //Moving Files
                final Path extractPath = resourcePackPath.resolve(extractor.getFirstArchiveContent().get(0).substring(1));
                startAndWait(new DirectoryContentMover(extractPath, resourcePath), "Moving files");

                KailexApp.GAME_SETTINGS.setActiveResourcepack("default_pack.zip");
            }
            catch (IOException e){
                throw new RuntimeException(e.getMessage(), e);
            }
            finally {
                readyLatch.countDown();
            }
        });
        action.start();

        return readyLatch;
    }

    /**
     * Applies a resourcepack and calls applyDefaultPack(...) method if pack is default pack.
     * Applying means to delete old assets in target resource folder, extract or move,
     * depending on whether pack is an archive or a folder, into target resource folder.
     * @param force if true method load pack in every case and if false pack will not be loaded if it is already loaded.
     * @return Returns latch which marks if applying is ready.
     * @throws IOException if an I/O error occurs or the parent directory does not exist or if method cannot wait for runnable.
     */
    public static CountDownLatch applyPack(Path pack, boolean force) throws IOException {
        if (pack.toFile().getName().equals("default_pack.zip")) return applyDefaultPack(force);

        final Path resourcePath = validate("resource", false);
        final CountDownLatch readyLatch = new CountDownLatch(1);

       if (!pack.toFile().exists()) throw new IllegalArgumentException(pack + " does not exists.");
       else if (!FileUtil.isPathEmpty(resourcePath)
                && !force
                && KailexApp.GAME_SETTINGS.getActiveResourcepack().equals(pack.toFile().getName())){
            LOGGER.log(Level.INFO, "Resourcepack is already selected.");
            readyLatch.countDown();
            return readyLatch;
        }

        clearAssets();

        Thread action = new Thread(() -> {
            try {
                //Delete old assets
                if (!FileUtil.isPathEmpty(resourcePath)) {
                    startAndWait(new DirectoryDeleter(resourcePath), "Deleting old files");
                    Files.createDirectory(resourcePath);
                }

                //Apply new assets
                final IProgressRunnable runnable;

                if (pack.toFile().isDirectory()) {
                    runnable = new DirectoryContentCopier(pack, resourcePath);
                }
                else {
                    if (!FileUtil.isArchive(pack)) throw new IOException("Pack is not a valid archive.");
                    runnable = new FileExtractor(pack, resourcePath);
                }

                startAndWait(runnable, "Applying Pack");

                KailexApp.GAME_SETTINGS.setActiveResourcepack(pack.toFile().getName());
            }
            catch (IOException e){
                throw new RuntimeException(e.getMessage(), e);
            }
            finally {
                readyLatch.countDown();
            }
        });
        action.start();

        return readyLatch;
    }

    /**
     * This method uses the Path#resolve(relPath) method beginning at home directory and returns its result and creates directory/file if it does not exist.
     * @throws IOException if an I/O error occurs or the parent directory does not exist.
     */
    public static Path validate(String relPath, boolean isFile) throws IOException {
        return FileUtil.validate(KailexApp.HOME_DIR, relPath, isFile);
    }

    /**
     * Starts IProgressRunnable and wait until finnished.
     * @param msg Message to loading screen.
     * @throws IOException if method cannot wait for runnable.
     */
    public static void startAndWait(IProgressRunnable runnable, String msg) throws IOException {
        CountDownLatch internLatch = monitorAction(runnable, msg);
        runnable.start();

        try {
            internLatch.await();
            assert !runnable.isFailure();
        }
        catch (InterruptedException e) {
            throw new IOException("Cannot: " + msg);
        }
    }

    /**
     * Checks periodically progress of monitorable action and put it with message into loading screen.
     * @return Returns CountDownLatch marking if actions is ready.
     */
    public static CountDownLatch monitorAction(IProgressRunnable runnable, String msg){
        if (actionMonitor != null){
            actionMonitor.forceStop();
            actionMonitor = null;
        }

        final CountDownLatch readyLatch = new CountDownLatch(1);

        actionMonitor = new Tickable() {
            @Override
            public void tick() {
                if (runnable.isReady()){
                    readyLatch.countDown();

                    if (runnable.isFailure()){
                        forceStop();
                        throw new RuntimeException("Cannot download resources.");
                    }

                    forceStop();
                }
                showProgress(msg, runnable.getPercentage());
            }
        };
        actionMonitor.start(0, 500);

        return readyLatch;
    }

    /**
     * Put data in LoadingScreenController if controller is active.
     * @param text Information about process.
     * @param progress [0.0,1.0] progress of progressbar (negative values are allowed -> fancy behaviour; values above 1.0 have same effect as 1.0)
     */
    public static void showProgress(@NotNull String text, int progress){
        if (KailexApp.activeController instanceof LoadingScreenController){
            LoadingScreenController controller = (LoadingScreenController) KailexApp.activeController.getInstance();

            Platform.runLater(() -> controller.setData(text, progress));
        }
    }

    public static void clearAssets(){
        //TODO: Implement
    }

    public static void loadAssets(){
        //TODO: Implement
    }
}
