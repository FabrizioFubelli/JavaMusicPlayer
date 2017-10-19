/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package ulg.play.media;

import javafx.application.Platform;
import ulg.play.players.AudioMediaPlayer;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MediaTimerTask extends TimerTask {

    private Timer mediaTimer = null;
    public static final Object timerLock = new Object();
    private WeakReference<AudioMediaPlayer> playerRef;

    public MediaTimerTask(AudioMediaPlayer player) {
        playerRef = new WeakReference<>(player);
    }

    public void start() {
        if (mediaTimer == null) {
            mediaTimer = new Timer(true);
            mediaTimer.scheduleAtFixedRate(this, 0, 100 /* period ms*/);
        }
    }

    public void stop() {
        if (mediaTimer != null) {
            mediaTimer.cancel();
            mediaTimer = null;
        }
    }

    @Override
    public void run() {
        synchronized (timerLock) {
            final AudioMediaPlayer player = playerRef.get();
            if (player != null) {
                Platform.runLater(() -> {
                    synchronized (timerLock) {
                        player.updateTime();
                    }
                });
            } else {
                cancel();
            }
        }
    }
}
