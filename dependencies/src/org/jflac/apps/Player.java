package org.jflac.apps;

/* libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2000,2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */


import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.jflac.PCMProcessor;
import org.jflac.FLACDecoder;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;


/**
 * Play a FLAC file application.
 * @author kc7bfi
 */
public class Player implements PCMProcessor {
    private final ExecutorService runningThread;

    private SourceDataLine line;
    private Vector<LineListener> listeners = new Vector<>();
    private boolean tryingForTest = false;

    public Player(ExecutorService runningThread) {
        this.runningThread = runningThread;
    }

    public void setTryingForTest(boolean b) {
        this.tryingForTest = b;
    }


    public void addListener (LineListener listener)
    {
        listeners.add(listener);
    }
    /**
     * Decode and play an input FLAC file.
     * @param inFileName    The input FLAC file name
     * @throws IOException  Thrown if error reading file
     * @throws LineUnavailableException Thrown if error playing file
     */
    public FLACDecoder decode(String inFileName) throws IOException, LineUnavailableException {
//        System.out.println("Play [" + inFileName + "]");
        FileInputStream is = new FileInputStream(inFileName);
        FLACDecoder decoder = new FLACDecoder(is);
        if (tryingForTest) {
            //System.out.println("Player -> start tryingForTest decode");
            decoder.addPCMProcessor(this);
            decoder.readMetadata();
        } else {
            runningThread.submit(() -> {
                decoder.addPCMProcessor(this);
                //System.out.println(Thread.currentThread().getName() + " -> is decoding audio");
                try {
                    decoder.decode();
                    //System.out.println(Thread.currentThread().getName() + " -> audio decoded");
                } catch (EOFException e) {
                    // skip
                } catch (IOException e) {
                    e.printStackTrace();
                }
                line.drain();
                line.close();
                //  We're going to clear out the list of listeners as well, so that everytime through
                //  things are basically at the same starting point.
                listeners.clear();
            });
        }
        return decoder;
    }

    /**
     * Process the StreamInfo block.
     * @param streamInfo the StreamInfo block
     * @see org.jflac.PCMProcessor#processStreamInfo(org.jflac.metadata.StreamInfo)
     */
    public void processStreamInfo(StreamInfo streamInfo) {
        try {
            AudioFormat fmt = streamInfo.getAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, AudioSystem.NOT_SPECIFIED);
            line = (SourceDataLine) AudioSystem.getLine(info);

            //  Add the listeners to the line at this point, it's the only
            //  way to get the events triggered.
            for (LineListener listener : listeners) line.addLineListener(listener);
            line.open(fmt, AudioSystem.NOT_SPECIFIED);
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the decoded PCM bytes.
     * @param pcm The decoded PCM data
     */
    public void processPCM(ByteData pcm) {
        line.write(pcm.getData(), 0, pcm.getLen());
    }

}
