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

import com.sun.javafx.tk.Toolkit;
import ulg.play.players.AudioMediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MediaPlayerShutdownHook implements Runnable {

    private final static List<WeakReference<AudioMediaPlayer>> playerRefs = new ArrayList<WeakReference<AudioMediaPlayer>>();
    private static boolean isShutdown = false;

    static {
        Toolkit.getToolkit().addShutdownHook(new MediaPlayerShutdownHook());
    }

    public static void addMediaPlayer(AudioMediaPlayer player) {
        synchronized (playerRefs) {
            if (isShutdown) {
                com.sun.media.jfxmedia.MediaPlayer jfxPlayer = player.retrieveJfxPlayer();
                if (jfxPlayer != null) {
                    jfxPlayer.dispose();
                }
            } else {
                for (ListIterator<WeakReference<AudioMediaPlayer>> it = playerRefs.listIterator(); it.hasNext();) {
                    AudioMediaPlayer l = it.next().get();
                    if (l == null) {
                        it.remove();
                    }
                }

                playerRefs.add(new WeakReference<AudioMediaPlayer>(player));
            }
        }
    }

    @Override
    public void run() {
        synchronized (playerRefs) {
            for (ListIterator<WeakReference<AudioMediaPlayer>> it = playerRefs.listIterator(); it.hasNext();) {
                AudioMediaPlayer player = it.next().get();
                if (player != null) {
                    player.destroyMediaTimer();
                    com.sun.media.jfxmedia.MediaPlayer jfxPlayer = player.retrieveJfxPlayer();
                    if (jfxPlayer != null) {
                        jfxPlayer.dispose();
                    }
                } else {
                    it.remove();
                }
            }

            isShutdown = true;
        }
    }
}

