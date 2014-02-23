/*
Copyright (c) 2013, Colorado State University
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

package galileo.comm;

import java.io.IOException;

import galileo.dht.NodeArray;
import galileo.event.EventType;
import galileo.event.GalileoEvent;

import galileo.serialization.SerializationException;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

/**
 * Represents a "Query Preamble" -- a set of information sent back to a client
 * before the query results start being transmitted.
 *
 * @author malensek
 */
public class QueryPreamble implements GalileoEvent {
    private String id;
    private String query;
    private NodeArray nodesInvolved;

    public QueryPreamble(String id, String query, NodeArray nodesInvolved) {
        this.id = id;
        this.query = query;
        this.nodesInvolved = nodesInvolved;
    }

    public String getQueryString() {
        return query;
    }

    public String getQueryId() {
        return id;
    }

    /**
     * Retrieves the list of StorageNodes involved in servicing a QueryRequest.
     * Each node in the list should reply with resulting metadata.
     */
    public NodeArray getNodesInvolved() {
        return nodesInvolved;
    }

    @Override
    public EventType getType() {
        return EventType.QUERY_PREAMBLE;
    }

    @Deserialize
    public QueryPreamble(SerializationInputStream in)
    throws IOException, SerializationException {
        id = in.readString();
        query = in.readString();
        nodesInvolved = new NodeArray(in);
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeString(id);
        out.writeString(query);
        out.writeSerializable(nodesInvolved);
    }
}
