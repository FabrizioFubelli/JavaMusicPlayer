/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * An {@link ActionEvent} representing a media marker. A marker is added to a
 * {@link Media} which is then played by a {@link ulg.play.players.AudioMediaPlayer}. The event
 * is fired when the playback position reaches the position of the marker.
 *
 * @see ulg.play.players.AudioMediaPlayer#onMarker
 * @since JavaFX 2.0
 */
public class MediaMarkerEvent extends ActionEvent {

    private static final long serialVersionUID = 20121107L;

    private Pair<String,Duration> marker;

    public MediaMarkerEvent(Pair<String,Duration> marker) {
        super();
        this.marker = marker;
    }

    /**
     * Retrieves the marker the event represents.
     *
     * @return the value of the marker.
     */
    public Pair<String,Duration> getMarker() {
        return marker;
    }
}
