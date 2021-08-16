package com.github.kailex.api.util.files;

/**
 * This interface marks all monitoring FileSystem actions and makes them monitorable.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public interface IProgressRunnable extends Runnable{
    /**
     * @return Returns if progress is ready.
     */
    boolean isReady();

    /**
     * @return Returns if progress has failed.
     */
    boolean isFailure();

    /**
     * @return Returns total processing size.
     */
    long processDataSize();

    /**
     * @return Returns actual processed size.
     */
    long getProcessedSize();

    /**
     * Starts progress.
     */
    void start();

    /**
     * @return Returns percentage of progress.
     */
    default int getPercentage(){
        final int result = (int) (100 * getProcessedSize() / (double) processDataSize());
        return (result > 100) ? 100 : Math.max(result, -1);
    }
}
