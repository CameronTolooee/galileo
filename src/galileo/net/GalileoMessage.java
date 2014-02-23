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

import java.io.IOException;

import java.nio.channels.SelectionKey;

import galileo.serialization.ByteSerializable;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

/**
 * The unit of data transmission in the Galileo DHT.  These packets are simple
 * in structure, containing a size prefix followed by the packet payload.
 *
 * @author malensek
 */
public class GalileoMessage implements ByteSerializable {

    private byte[] payload;

    public SelectionKey key;

    /**
     * Constructs a GalileoMessage from an array of bytes.
     *
     * @param payload message payload in the form of a byte array.
     */
    public GalileoMessage(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Constructs a GalileoMessage from an array of bytes with an associated
     * {@link SelectionKey} of the message source.
     *
     * @param payload message payload in the form of a byte array.
     * @param key SelectionKey of the message source.
     */
    public GalileoMessage(byte[] payload, SelectionKey key) {
        this.payload = payload;
        this.key = key;
    }

    /**
     * Retrieves the payload for this GalileoMessage.
     *
     * @return the GalileoMessage payload
     */
    public byte[] getPayload() {
        return payload;
    }

    public SelectionKey getSelectionKey() {
        return key;
    }

    /**
     * Constructs a new GalileoMessage from a serialization stream.
     */
    public GalileoMessage(SerializationInputStream in)
    throws IOException {
        int dataSize = in.readInt();
        payload = new byte[dataSize];
        in.read(payload);
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
    }
}
