
package galileo.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import galileo.dht.NetworkConfig;
import galileo.dht.NodeInfo;
import galileo.event.EventType;
import galileo.event.GalileoEvent;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

public class ElectionEvent implements GalileoEvent {

    private NodeInfo respondTo;

    public ElectionEvent() throws UnknownHostException {
        respondTo = new NodeInfo(InetAddress.getLocalHost().getHostName(),
                NetworkConfig.DEFAULT_PORT);
    }

    public NodeInfo respondTo() {
        return respondTo;
    }

    @Deserialize
    public ElectionEvent(SerializationInputStream in) throws IOException {
        respondTo = new NodeInfo(in);
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeSerializable(respondTo);
    }

    @Override
    public EventType getType() {
        return EventType.ELECTION;
    }

}
