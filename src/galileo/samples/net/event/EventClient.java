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

package galileo.samples.net.event;

import java.io.IOException;

import java.util.List;

import galileo.event.EventContext;
import galileo.event.EventHandler;
import galileo.event.EventProducer;
import galileo.event.EventReactor;
import galileo.net.ClientMessageRouter;
import galileo.net.NetworkDestination;

public class EventClient {

    private NetworkDestination server;

    private ClientMessageRouter messageRouter;
    private EventReactor eventReactor;
    private EventProducer eventProducer;

    public EventClient(NetworkDestination server)
    throws IOException {
        this.server = server;

        eventReactor = new EventReactor(this, SampleEventMap.instance());
        messageRouter = new ClientMessageRouter();
        eventProducer = new EventProducer(messageRouter, eventReactor);

        messageRouter.addListener(eventReactor);
    }

    public void generateEvents()
    throws Exception {
        GoodEvent ge = new GoodEvent();
        eventProducer.publishEvent(server, ge);

        BadEvent be = new BadEvent();
        eventProducer.publishEvent(server, be);

        /* Have the EventReactor wait for a reply.  Note that this behavior is
         * only shown to simplify the flow of events; normally, an event loop
         * should be processing the next event. */
        eventReactor.processNextEvent();

        UglyEvent ue = new UglyEvent("Hello", "World");
        eventProducer.publishEvent(server, ue);
    }

    @EventHandler
    public void processReply(BadReplyEvent event, EventContext context) {
        System.out.println("A BadReply has been received!");
        List<String> strings = event.getStringList();
        System.out.println("Reply contents:");
        for (String s : strings) {
            System.out.println("'" + s + "'");
        }
    }

    public static void main(String[] args)
    throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: galileo.samples.net.event.EventClient "
                    + "<server hostname>");
            System.exit(1);
        }

        NetworkDestination server = new NetworkDestination(
                args[0], EventServer.LISTEN_PORT);

        EventClient ec = new EventClient(server);
        ec.generateEvents();
    }
}
