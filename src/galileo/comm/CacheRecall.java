
package galileo.comm;

import java.io.IOException;

import galileo.event.EventType;
import galileo.event.GalileoEvent;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

public class CacheRecall implements GalileoEvent {

    private String qid;

    public CacheRecall(String qid) {
        this.qid = qid;
    }

    public String getQID() {
        return qid;
    }

    public CacheRecall(SerializationInputStream in) throws IOException {
        this.qid = in.readString();
    }

    @Override
    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeString(qid);
    }

    @Override
    public EventType getType() {
        return EventType.CACHE_RECALL;
    }

}
