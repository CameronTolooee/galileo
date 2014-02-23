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

import galileo.client.EventPublisher;
import galileo.comm.Disconnection;

import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Provides client-side message routing capabilities. This includes connecting
 * to a remote server, and transmitting messages in non-blocking mode.
 *
 * @author malensek
 */
public class ClientMessageRouter extends MessageRouter {

    protected Map<SocketChannel, NetworkDestination> connections
        = new HashMap<>();
    protected Map<NetworkDestination, SocketChannel> connectedHosts
        = new HashMap<>();

    protected BlockingQueue<SocketChannel> pendingRegistrations
        = new LinkedBlockingQueue<>();
    protected Map<SocketChannel, BlockingQueue<SelectionKey>> waitingKeys
        = new ConcurrentHashMap<>();

    public ClientMessageRouter()
    throws IOException {
        super();
        initializeSelector();
    }

    public ClientMessageRouter(int readBufferSize, int maxWriteQueueSize)
    throws IOException {
        super(readBufferSize, maxWriteQueueSize);
        initializeSelector();
    }

    private void initializeSelector()
    throws IOException {
        this.selector = Selector.open();
        this.online = true;
        Thread selectorThread = new Thread(this);
        selectorThread.start();
    }

    /**
     * Connects to a server.
     *
     * @param hostname name of the host to connect to
     * @param port port on the destination host to connect to
     */
    public NetworkDestination connectTo(String hostname, int port)
    throws IOException {
        return connectTo(new NetworkDestination(hostname, port));
    }
 
    /**
     * Connects to a server at the specified network destination.
     *
     * @param destination NetworkDestination to connect to.
     */
    public NetworkDestination connectTo(NetworkDestination destination)
    throws IOException {
        if (connectedHosts.containsKey(destination)) {
            return destination;
        }

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(
                destination.getHostname(), destination.getPort());
        channel.connect(address);

        pendingRegistrations.offer(channel);
        waitingKeys.put(channel, new LinkedBlockingQueue<SelectionKey>());
        connectedHosts.put(destination, channel);
        connections.put(channel, destination);
        return destination;
    }

    /**
     * Handles pending registration operations on the Selector thread.
     */
    private void processPendingRegistrations()
    throws ClosedChannelException {
        if (pendingRegistrations.size() == 0) {
            return;
        }

        List<SocketChannel> registrations = new ArrayList<>();
        pendingRegistrations.drainTo(registrations);

        for (SocketChannel channel : registrations) {
            TransmissionTracker tracker
                = new TransmissionTracker(writeQueueSize);

            channel.register(selector, SelectionKey.OP_CONNECT, tracker);
        }
    }

    @Override
    public void run() {
        while (online) {
            try {
                processPendingRegistrations();
                processSelectionKeys();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void connect(SelectionKey key) {
        super.connect(key);
        waitingKeys.get((SocketChannel) key.channel()).offer(key);
    }

    /**
     * Shuts down the message processor and disconnects from the server(s).
     */
    public void shutdown() {
        for (SocketChannel channel : connectedHosts.values()) {
            SelectionKey key = channel.keyFor(this.selector);
            if (key != null) {
                disconnect(key);
            }
        }
        this.online = false;
        selector.wakeup();
    }

    /**
     * Broadcasts a message to the connected servers.
     */
    public void broadcastMessage(GalileoMessage message)
    throws IOException {
        for (SocketChannel channel : connectedHosts.values()) {
            sendMessage(channel.keyFor(this.selector), message);
        }
    }

    /**
     * Sends a message to the specified network destination.  Connections are
     * completed lazily during the first send operation.
     */
    public void sendMessage(NetworkDestination destination,
            GalileoMessage message)
    throws IOException {

        SocketChannel channel = connectedHosts.get(destination);
        SelectionKey key = channel.keyFor(this.selector);
        if (key == null) {
            if (!pendingRegistrations.contains(channel)) {
                throw new IOException("Not connected to destination: "
                        + destination);
            } else {
                this.selector.wakeup();

                try {
                    key = waitingKeys.get(channel).take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Sender thread interrupted.");
                }

                if (!key.isValid()) {
                    throw new IOException("Connection refused.");
                }
            }
        }

        sendMessage(key, message);
    }

    /**
     * Inform subscribed MessageListener instances that the connection to the
     * remote server has been terminated.
     *
     * @param key SelectionKey for the SocketChannel that was disconnected.
     */
    @Override
    protected void disconnect(SelectionKey key) {
        super.disconnect(key);
        SocketChannel channel = (SocketChannel) key.channel();
        NetworkDestination destination = connections.get(channel);

        Disconnection disconnect = new Disconnection(destination);

        GalileoMessage message = null;
        try {
            message = EventPublisher.wrapEvent(disconnect);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not create Disconnect event.", e);
        }

        connectedHosts.remove(destination);
        connections.remove(channel);

        super.dispatchMessage(message);
    }
}
