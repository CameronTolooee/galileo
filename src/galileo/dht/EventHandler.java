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

package galileo.dht;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SelectionKey;

import java.util.logging.Level;
import java.util.logging.Logger;

import galileo.client.EventPublisher;
import galileo.event.EventContainer;
import galileo.event.GalileoEvent;

import galileo.net.ClientConnectionPool;
import galileo.net.GalileoMessage;
import galileo.net.MessageRouter;

import galileo.serialization.ByteSerializable;
import galileo.serialization.SerializationException;
import galileo.serialization.Serializer;

public abstract class EventHandler implements ProcessingUnit {

    private static final Logger logger = Logger.getLogger("galileo");

    public GalileoMessage message;
    public EventContainer eventContainer;
    public MessageRouter router;
    public ClientConnectionPool connectionPool;

    public EventHandler() { }

    public void run() {
        if (eventContainer == null) {
            logger.warning("Received null event");
            return;
        }

        if (message == null) {
            logger.warning("Reference message is null");
            return;
        }

        try {
            handleEvent();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to process exchange packet!", e);
        }
    }

    /**
     * Publishes a response to the event that triggered this handler.
     */
    protected void publishResponse(GalileoEvent event)
    throws IOException {
        GalileoMessage response = EventPublisher.wrapEvent(event);
        router.sendMessage(message.getSelectionKey(), response);
    }

    /**
     * Publishes an event to the specified StorageNode.
     */
    protected void publishEvent(GalileoEvent event, NodeInfo destination)
    throws IOException {
        GalileoMessage message = EventPublisher.wrapEvent(event);
        connectionPool.connectTo(destination);
        connectionPool.sendMessage(destination, message);
    }

    /**
     * Publishes an event to the specified SelectionKey.
     */
    protected void publishEvent(GalileoEvent event, SelectionKey key)
    throws IOException {
        router.sendMessage(key, EventPublisher.wrapEvent(event));
    }

    /**
     * Sends a message on the specified SelectionKey.
     */
    protected void sendMessage(GalileoMessage message, SelectionKey key)
    throws IOException {
        router.sendMessage(key, message);
    }

    protected <T extends ByteSerializable> T deserializeEvent(Class<T> type)
    throws IOException, SerializationException {
        return Serializer.deserialize(type, eventContainer.getEventPayload());
    }

    public abstract void handleEvent() throws Exception;
}
