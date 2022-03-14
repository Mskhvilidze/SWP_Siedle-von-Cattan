package de.uol.swp.server.game.session;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A custom Timer that can be restarted
 */
public class TurnTimer {
    private Timer timer = new Timer();
    private long timerDuration;
    private boolean alive = true;

    /**
     * Constructor
     *
     * @param timerDuration the default duration of the timer
     * @param timeUnit      the time unit of the timerDuration parameter
     */
    public TurnTimer(int timerDuration, TimeUnit timeUnit) {
        this.timerDuration = timeUnit.toMillis(timerDuration);
    }

    /**
     * Restart this timer with the given TimerTask
     *
     * @param task the TimerTask that should be started
     */
    public void restartTimer(TimerTask task) {
        restartTimer(task, timerDuration, TimeUnit.MILLISECONDS);
    }

    /**
     * Restart this timer with the given TimerTask and the given duration
     *
     * @param task          the TimerTask that should be started
     * @param timerDuration the duration of the timer
     * @param timeUnit      the time unit of the timerDuration parameter
     */
    public void restartTimer(TimerTask task, long timerDuration, TimeUnit timeUnit) {
        timer.cancel();
        if (alive) {
            timer = new Timer();
            timer.scheduleAtFixedRate(task, timeUnit.toMillis(timerDuration), timeUnit.toMillis(timerDuration));
        }
    }

    /**
     * Stops this timer
     */
    public void stop() {
        timer.cancel();
        timer.purge();
    }

    /**
     * Kills this timer. Ensures that no task is started anymore
     */
    public void kill() {
        alive = false;
    }

    /**
     * Sets the default duration of this timer
     *
     * @param timerDuration the duration of the timer
     * @param timeUnit      the time unit of the timerDuration parameter
     */
    public void setTimerDuration(int timerDuration, TimeUnit timeUnit) {
        this.timerDuration = timeUnit.toMillis(timerDuration);
    }
}
