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

package galileo.dht.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Table tracks cached queries and their associative cached data. To be
 * consulted upon receiving re-query of a cached query.
 * 
 * @author Cameron Tolooee
 * 
 */
public class CachedBufferTable {

    /* Maps query id to the buffer */
    Map<String, FixedBuffer<String>> table;

    public CachedBufferTable() {
        table = new ConcurrentHashMap<String, FixedBuffer<String>>();
    }

    /**
     * Returns the table entry for the specified query id.
     * 
     * @param qid
     *            String identifier of the cached query
     * @return FixedBuffer of data associated with the QID
     */
    public FixedBuffer<String> getBuffer(String qid) {
        return table.get(qid);
    }

    public void addEntry(String qid, FixedBuffer<String> buffer) {
        table.put(qid, buffer);
    }

    public void addEntry(String qid, int bufferSize) {
        addEntry(qid, new FixedBuffer<String>(bufferSize));
    }

    public void remove(String qid) {
        table.remove(qid);
    }

    public Set<Entry<String, FixedBuffer<String>>> entrySet() {
        return table.entrySet();
    }

    public String toString() {
        String str = String.format("%10s%3s%10s", "Query ID", "|", "Buffer"
                + System.lineSeparator());
        str += "-------------------------" + System.lineSeparator();
        for (Entry<String, FixedBuffer<String>> entry : table.entrySet()) {
            str += String.format("%10s | ", entry.getKey());
            str += String.format("%s",
                    entry.getValue().toString() + System.lineSeparator());
        }
        return str;
    }
}
