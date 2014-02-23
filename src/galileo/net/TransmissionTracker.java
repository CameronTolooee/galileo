/*
Copyright (c) 2013, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package galileo.net;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Tracks transmission processing operations; helps convert TCP streams into
 * individual messages that can be consumed by the system or client
 * applications.
 *
 * @author malensek
 */
public class TransmissionTracker {

    /** Contains a list of pending write operations for this client. */
    private BlockingQueue<ByteBuffer> pendingWrites;

    /** Read pointer for the message size prefix */
    public int prefixPointer;

    /** Array to store payload size prefix information */
    public byte[] prefix = new byte[4];

    /** Read pointer for the message payload */
    public int readPointer;

    /** Array to store received payload data */
    public byte[] payload;

    /** The size of the complete payload in bytes */
    public int expectedBytes;

    public TransmissionTracker(int writeQueueSize) {
        pendingWrites = new LinkedBlockingQueue<>(writeQueueSize);
    }

    /**
     * Allocates a buffer for the incoming payload once the message size prefix
     * has been read.
     */
    public void allocatePayload() {
        payload = new byte[expectedBytes];
    }

    /**
     * Restores the TransmissionTracker to its original state, ready to process
     * another message from a stream.
     */
    public void resetCounters() {
        prefixPointer = 0;
        readPointer = 0;
        expectedBytes = 0;
    }

    public BlockingQueue<ByteBuffer> getPendingWriteQueue() {
        return pendingWrites;
    }

    /**
     * Retrieves a TransmissionTracker attachment from a SelectionKey.
     */
    public static TransmissionTracker fromKey(SelectionKey key) {
        return (TransmissionTracker) key.attachment();
    }
}
