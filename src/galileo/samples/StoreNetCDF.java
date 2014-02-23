package galileo.samples;

import java.io.IOException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import galileo.client.EventPublisher;
import galileo.comm.StorageRequest;
import galileo.dataset.Block;
import galileo.dataset.Metadata;
import galileo.net.ClientMessageRouter;
import galileo.net.GalileoMessage;
import galileo.net.MessageListener;
import galileo.net.NetworkDestination;

public class StoreNetCDF implements MessageListener {

    private ClientMessageRouter messageRouter;
    public EventPublisher publisher;

    public StoreNetCDF() throws IOException {
        messageRouter = new ClientMessageRouter();
        publisher = new EventPublisher(messageRouter);

        messageRouter.addListener(this);
    }

    public NetworkDestination connect(String hostname, int port)
    throws UnknownHostException, IOException {
        return messageRouter.connectTo(hostname, port);
    }

    public void disconnect() {
        messageRouter.shutdown();
    }

    @Override
    public void onMessage(GalileoMessage message) {
        if (message == null) {
            /* Connection was terminated */
            messageRouter.shutdown();
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        String serverHostName = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String fileName = args[1];

        StoreNetCDF client = new StoreNetCDF();
        NetworkDestination server = client.connect(serverHostName, serverPort);

        List<Block> blocks = new ArrayList<>();
        Map<String, Metadata> metas = ConvertNetCDF.readFile(fileName);
        for (Map.Entry<String, Metadata> entry : metas.entrySet()) {
            blocks.add(ConvertNetCDF.createBlock("", entry.getValue()));
        }

        StorageRequest store = new StorageRequest(blocks.get(0));
        client.publisher.publish(server, store);
    }
}
