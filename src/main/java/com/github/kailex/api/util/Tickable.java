package com.github.kailex.api.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents an Object with Timer.
 *
 * @author Alexander Ley
 * @version 1.5
 */
public abstract class Tickable {

    private boolean isRunning = false;
    private boolean isReady = true;

    protected Timer timer = new Timer();
    protected TimerTask task = new TimerTask() {
        @Override
        public void run() {
            checkTick();
        }
    };

    /**
     * Task Loop
     */
    public abstract void tick();

    /**
     * Stop timer in loop if requested by stop.
     */
    public void checkTick(){
        if (!isRunning) forceStop();
        else tick();
    }

    public boolean isRunning(){
        return isRunning;
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * Starts the Timer
     * @param delay Start delay.
     * @param period Timer period.
     * @throws IllegalStateException if timer is already started.
     */
    public void start(long delay, long period){
        if (!isReady) throw new IllegalStateException("Timer is already started.");

        isRunning = true;
        isReady = false;

        timer.schedule(task, delay, period);
    }

    /**
     * Stops Timer on next tick, so that tick() can run to end and in next tick timer will be closed.
     */
    public void stop(){
        isRunning = false;
    }

    /**
     * Stops Timer immediately. If tick() is running it will be run to end.
     */
    public void forceStop() {
        timer.cancel();
        isRunning = false;
    }

    /**
     * Tries to refresh timer object, so that it can start again.
     * It is only refresh-able if the timer was at least one time started and stopped.
     * Note Refreshing timer lets application open.
     */
    public void refresh(){
        if (!isRunning) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            };
            isReady = true;
        }
    }
}
