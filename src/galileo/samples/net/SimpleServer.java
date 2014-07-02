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

import galileo.net.GalileoMessage;
import galileo.net.MessageListener;
import galileo.net.NetworkDestination;
import galileo.net.ServerMessageRouter;

/**
 * Very simple demo server that receives and prints String-based messages from
 * clients.
 */
public class SimpleServer implements MessageListener {

    public static final int SERVER_PORT = 7777;

    private ServerMessageRouter messageRouter = new ServerMessageRouter();

    public SimpleServer()
    throws IOException {
        messageRouter.listen(SERVER_PORT);
        messageRouter.addListener(this);
        System.out.println("Listening for incoming messages...");
    }

    @Override
    public void onConnect(NetworkDestination endpoint) { }

    @Override
    public void onDisconnect(NetworkDestination endpoint) { }

    @Override
    public void onMessage(GalileoMessage message) {
        /* Print out the message we received */
        System.out.println(new String(message.getPayload()));
    }

    public static void main(String[] args)
    throws Exception {
        new SimpleServer();
    }
}
