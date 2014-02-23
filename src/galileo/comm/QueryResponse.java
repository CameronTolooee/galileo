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

import galileo.event.EventType;
import galileo.event.GalileoEvent;
import galileo.graph.GraphException;
import galileo.graph.MetadataGraph;
import galileo.serialization.SerializationException;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

public class QueryResponse implements GalileoEvent {

    private String id;
    private MetadataGraph metadata;

    public QueryResponse(String id, MetadataGraph metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public MetadataGraph getMetadata() {
        return metadata;
    }

    @Override
    public EventType getType() {
        return EventType.QUERY_RESPONSE;
    }

    public QueryResponse(SerializationInputStream in)
    throws IOException, SerializationException, GraphException {
        id = in.readString();
        metadata = new MetadataGraph(in);
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeString(id);
        out.writeSerializable(metadata);
    }
}
