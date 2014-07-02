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

package galileo.samples.net.synopsis;

import java.io.IOException;

import java.math.BigInteger;

import galileo.event.EventContext;
import galileo.event.EventHandler;
import galileo.event.EventReactor;
import galileo.event.EventWithSynopsis;
import galileo.event.SynopsisWrapper;
import galileo.net.ServerMessageRouter;

public class SynopsisServer {

    public static final int DEFAULT_PORT = 7777;

    private int port;

    private ServerMessageRouter messageRouter;
    private EventReactor eventReactor;

    public SynopsisServer(int port) {
        this.port = port;
    }

    public void start()
    throws IOException {
        System.out.println("Starting up.");
        eventReactor = new EventReactor(this, new SynopsisWrapper());
        messageRouter = new ServerMessageRouter();
        messageRouter.addListener(eventReactor);
        messageRouter.listen(this.port);
        System.out.println("Listening on port " + this.port);

        /* Process events as they come in */
        while (true) {
            try {
                eventReactor.processNextEvent();
            } catch (Exception e) {
                System.out.println("Error processing event!");
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void processEvent(EventWithSynopsis event, EventContext context)
    throws IOException {
        String synopsis = event.getSynopsis();
        byte[] payload = event.getPayload();

        System.out.println("Got an event. Synopsis: " + synopsis);

        if (synopsis.equals("SynopsisA")) {

            System.out.println("Its length is: " + payload.length);

        } else if (synopsis.equals("SynopsisB")) {

            BigInteger bi = new BigInteger(payload);
            System.out.println("Hex representation: " + bi.toString(16));

        } else if (synopsis.equals("SynopsisC")) {

            System.out.println("String representation: " + new String(payload));
            System.out.println("We will send a reply.");
            EventWithSynopsis reply = new EventWithSynopsis(
                    "ReplySynopsis", "Hello World!".getBytes());
            context.sendReply(reply);

        } else if (synopsis.equals("ShutdownRequest")) {

            EventWithSynopsis reply = new EventWithSynopsis(
                    "Shutdown", new byte[0]);
            context.sendReply(reply);

        } else {
            System.out.println("I don't know what to do with this "
                + "message synopsis!");
        }
    }

    public static void main(String[] args)
    throws IOException {
        SynopsisServer server = new SynopsisServer(DEFAULT_PORT);
        server.start();
    }
}
