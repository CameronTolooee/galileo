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

package galileo.comm;

import galileo.event.EventType;
import galileo.event.GalileoEvent;
import galileo.query.Query;
import galileo.serialization.ByteSerializable;
import galileo.serialization.SerializationException;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

import java.io.IOException;

import org.joda.time.Interval;

/**
 * Encapsulates a cached query in Galileo. Cached queries will be reissued
 * periodically and have results cached for quick access upon re-querying
 * 
 * @author Cameron Tolooee
 * 
 */
public class CachedQueryEvent implements GalileoEvent {

    private String id;
    private long expiration; // when to terminate dedicated thread
    private int updateInterval; // interval to reissue Q and update trees
    private Query query;

    public CachedQueryEvent(String id, long expiration, int updateInterval,
            Query query) {
        this.id = id;
        this.expiration = expiration;
        this.updateInterval = updateInterval;
        this.query = query;
    }

    public String getID() {
        return id;
    }

    public Query getQuery() {
        return query;
    }

    public int getInterval() {
        return updateInterval;
    }

    public long getExpiration() {
        return expiration;
    }

    public CachedQueryEvent(SerializationInputStream in) throws IOException,
            SerializationException {
        id = in.readString();
        expiration = in.readLong();
        updateInterval = in.readInt();
        query = new Query(in);
    }

    public void serialize(SerializationOutputStream out) throws IOException {
        out.writeString(id);
        out.writeLong(expiration);
        out.writeInt(updateInterval);
        out.writeSerializable(query);
    }

    public String toString() {
        String str = "Cached Query Event: " + query.toString() + " Query id: "
                + id + " TTL: " + expiration + "Update Interval: "
                + updateInterval;
        return str;
    }

    @Override
    public EventType getType() {
        return EventType.CACHED_QUERY;
    }

}
