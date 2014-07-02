/*
Copyright (c) 2014, Colorado State University
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

package galileo.samples.net;

import java.io.IOException;

import java.util.Random;

import galileo.net.IOMessageRouter;
import galileo.net.GalileoMessage;
import galileo.net.MessageContext;
import galileo.net.MessageListener;
import galileo.net.NetworkDestination;

/**
 * Demonstration of a class that acts as both a client and server: it listens
 * for messages on a specific port while also periodically sending messages to
 * another server.
 * <p>
 * Sample usage:<br>
 * galileo.samples.net.ClientServer 7000 lattice-0 7001 hello <br>
 * galileo.samples.net.ClientServer 7001 lattice-1 7000 hola!
 */
public class ClientServer implements MessageListener {

    /** The port we will listen on. */
    private int port;

    /** The remove server we will connect to */
    private NetworkDestination server;

    private Random random = new Random();
    private IOMessageRouter messageRouter;

    public ClientServer(int port, NetworkDestination server) {
        this.port = port;
        this.server = server;
    }

    public void start(String data)
    throws IOException {
        /* Bind to the port we'll listen for messages on. */
        messageRouter = new IOMessageRouter();
        messageRouter.listen(this.port);
        messageRouter.addListener(this);

        /* Start sending messages to the other server */
        while (true) {
            byte[] messageData = data.getBytes();
            GalileoMessage message = new GalileoMessage(messageData);

            messageRouter.sendMessage(server, message);

            try {
                Thread.sleep(random.nextInt(5000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnect(NetworkDestination endpoint) { }

    @Override
    public void onDisconnect(NetworkDestination endpoint) { }

    @Override
    public void onMessage(GalileoMessage message) {
        MessageContext context = message.getContext();
        System.out.println("Got message on port " + context.getServerPort()
                + " from " + context.getSource() + ": "
                + new String(message.getPayload()));
    }

    public static void main(String[] args)
    throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: galileo.samples.net.ClientServer "
                    + "<local port> <server hostname> <server port> <msg>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        NetworkDestination server = new NetworkDestination(
                args[1], Integer.parseInt(args[2]));

        ClientServer cs = new ClientServer(port, server);
        cs.start(args[3]);
    }
}
