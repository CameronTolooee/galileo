package galileo.samples.net.synopsis;

import java.io.IOException;

import java.util.Random;

import galileo.event.ConcurrentEventReactor;
import galileo.event.EventContext;
import galileo.event.EventHandler;
import galileo.event.EventWithSynopsis;
import galileo.event.SynopsisWrapper;
import galileo.net.ClientMessageRouter;
import galileo.net.NetworkDestination;

public class SynopsisClient {

    private NetworkDestination server;

    private ClientMessageRouter messageClient;
    private ConcurrentEventReactor eventReactor;

    public SynopsisClient(NetworkDestination server)
    throws IOException {
        this.server = server;
        eventReactor = new ConcurrentEventReactor(
                this, new SynopsisWrapper(), 1);
        eventReactor.start();
        messageClient = new ClientMessageRouter();
        messageClient.addListener(eventReactor);
    }

    public void sendMessages()
    throws IOException {
        Random random = new Random();

        /* Send a simple, empty message */
        EventWithSynopsis event1 = new EventWithSynopsis(
                "SynopsisA", new byte[3]);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event1));

        /* How about some random data? */
        byte[] data = new byte[8192];
        random.nextBytes(data);
        EventWithSynopsis event2 = new EventWithSynopsis("SynopsisA", data);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event2));


        /* Send some random bytes on SynopsisB.  The server will print out the
         * hex representation of the data. */
        byte[] smallData = new byte[100];
        random.nextBytes(smallData);
        EventWithSynopsis event3
            = new EventWithSynopsis("SynopsisB", smallData);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event3));

        /* Send something on SynopsisC.  This should make the server send us a
         * reply. */
        EventWithSynopsis event4
            = new EventWithSynopsis("SynopsisC", new byte[1]);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event4));

        /* How about a synopsis the server won't know about? */
        EventWithSynopsis event5
            = new EventWithSynopsis("BlahSynopsis", new byte[1]);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event5));

        /* Send something on a special synopsis, 'ShutdownRequest'.  This will
         * make the server send us a 'Shutdown' reply that we will obey */
        EventWithSynopsis event6
            = new EventWithSynopsis("ShutdownRequest", new byte[1]);
        messageClient.sendMessage(server, eventReactor.wrapEvent(event6));
    }

    @EventHandler
    public void processEvent(EventWithSynopsis event, EventContext context)
    throws IOException {
        String synopsis = event.getSynopsis();

        System.out.println("We got a reply from the server!");
        System.out.println("Synopsis: " + synopsis);

        if (synopsis.equals("Shutdown")) {
            System.out.println("Goodbye!");
            messageClient.shutdown();
            System.exit(1);
        }
    }

    public static void main(String[] args)
    throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: galileo.samples.net.SynopsisClient "
                    + "<server hostname>");
            System.exit(1);
        }

        String hostname = args[0];
        NetworkDestination server = new NetworkDestination(
                hostname, SynopsisServer.DEFAULT_PORT);

        SynopsisClient client = new SynopsisClient(server);
        client.sendMessages();
    }
}
