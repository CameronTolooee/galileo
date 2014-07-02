
package galileo.dht.cache;

import galileo.comm.Disconnection;
import galileo.comm.QueryResponse;
import galileo.event.EventContainer;
import galileo.event.EventType;
import galileo.net.GalileoMessage;
import galileo.net.MessageListener;
import galileo.serialization.Serializer;

import java.util.HashSet;
import java.util.Set;

public class CachedQueryListener implements MessageListener {

    private CachedQueryWorker worker;
    private int responseCount;
    private Set<String> list;

    public CachedQueryListener(CachedQueryWorker worker) {
        this.worker = worker;
        this.responseCount = 0;
        this.list = new HashSet<String>();
    }

    public Set<String> getList() {
        return list;
    }

    @Override
    public void onMessage(GalileoMessage message) {
        if (message == null) {
            /* Connection was terminated */
            return;
        }

        try {
            EventContainer container = Serializer.deserialize(
                    EventContainer.class, message.getPayload());
            System.out.println("Cached Listener: " + container.getEventType());
            if (container.getEventType() == EventType.QUERY_RESPONSE) {
                ++responseCount;
                QueryResponse response = Serializer.deserialize(
                        QueryResponse.class, container.getEventPayload());
                for (String name : response.getMetadata().getCanonnicalNames()) {
                    list.add(name);
                }
                if (responseCount == 1) {
                    responseCount = 0;
                    synchronized (worker) {
                        worker.notify();
                    }
                }
            }

            if (container.getEventType() == EventType.DISCONNECT) {
                Disconnection disconnect = Serializer.deserialize(
                        Disconnection.class, container.getEventPayload());

                System.out.println("Disconnected from " + disconnect);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not read event container");
        }
    }

}
