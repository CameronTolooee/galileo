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

import galileo.event.EventContext;
import galileo.event.EventHandler;
import galileo.event.EventReactor;
import galileo.net.ServerMessageRouter;

public class EventServer {

    public static final int LISTEN_PORT = 7777;

    private int port;

    private ServerMessageRouter messageRouter;

    private EventReactor eventReactor;

    public EventServer(int port) {
        this.port = port;
    }

    public void start()
    throws IOException {
        eventReactor = new EventReactor(this, SampleEventMap.instance());
        messageRouter = new ServerMessageRouter();
        messageRouter.addListener(eventReactor);
        messageRouter.listen(this.port);

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
    public void processGoodEvent(GoodEvent event, EventContext context) {
        System.out.println("We got a GoodEvent!");
        System.out.println("Let's see what it says.");
        System.out.println("Contents: " + event.getData());
    }

    @EventHandler
    public void processBadEvent(BadEvent event, EventContext context) {
        System.out.println("We got a BadEvent! Oh no.");
        System.out.println("Badness level: " + event.getBadness());

        System.out.println("We'll send a reply.");
        try {
            context.sendReply(new BadReplyEvent(
                        "Lorem ipsum",
                        "Dolor sit amet",
                        "Consectetur adipiscing elit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void processUglyEvent(UglyEvent event, EventContext context) {
        System.out.println("An ugly event! Let's shut down.");
        System.out.println("----");
        System.out.println(event.getFirstString());
        System.out.println(event.getSecondString());
        System.out.println("----");
        System.out.println("Bye!");
        System.exit(0);
    }

    public static void main(String[] args)
    throws IOException {
        EventServer server = new EventServer(LISTEN_PORT);
        server.start();
    }
}
