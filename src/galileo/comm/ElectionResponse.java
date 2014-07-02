
package galileo.comm;

import java.io.IOException;

import galileo.dht.NodeInfo;
import galileo.event.EventType;
import galileo.event.GalileoEvent;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

public class ElectionResponse implements GalileoEvent {

    private long freeMem;
    private long totalMem;
    private NodeInfo node;

    public ElectionResponse(long freeMem, long totalMem, NodeInfo hostname) {
        this.freeMem = freeMem;
        this.totalMem = totalMem;
        this.node = hostname;
    }

    public NodeInfo getNode() {
        return node;
    }

    public long freeMemory() {
        return freeMem;
    }

    public long totalMemory() {
        return totalMem;
    }

    @Deserialize
    public ElectionResponse(SerializationInputStream in) throws IOException {
        freeMem = in.readLong();
        totalMem = in.readLong();
        node = new NodeInfo(in);
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeLong(freeMem);
        out.writeLong(totalMem);
        out.writeSerializable(node);
    }

    @Override
    public EventType getType() {
        return EventType.ELECTION_RESPONSE;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElectionResponse other = (ElectionResponse) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (node.getHostname().equals(other.node.getHostname())
                && node.getPort() == other.node.getPort())
            return true;
        return false;
    }

}
