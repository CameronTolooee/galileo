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

import galileo.client.EventPublisher;
import galileo.comm.QueryRequest;
import galileo.net.ClientMessageRouter;
import galileo.net.NetworkDestination;
import galileo.query.Query;

import java.io.IOException;
import java.util.Collection;

/**
 * Thread repsonsible for re-issuing cached queries, and updating the
 * <code>StorageNode</code>'s <code>CachedBufferTable</code> with results. Only
 * results that are new since the last iteration are stored.
 * 
 * @author Cameron Tolooee
 * 
 */
public class CachedQueryWorker implements Runnable {

    private Query query;
    private String qid;
    private CachedBufferTable table;
    private ClientMessageRouter messageRouter;
    private EventPublisher publisher;
    private CachedQueryListener listener;
    private boolean pendingResponse;
    private boolean canceled;

    public CachedQueryWorker(Query query, String qid, CachedBufferTable table)
            throws IOException {
        this.query = query;
        this.qid = qid;
        this.table = table;
        this.messageRouter = new ClientMessageRouter();
        this.listener = new CachedQueryListener(this);
        messageRouter.addListener(listener);
        publisher = new EventPublisher(messageRouter);
        NetworkDestination dest = new NetworkDestination("localhost", 5555);
        messageRouter.connectTo(dest);
        canceled = false;
    }

    @Override
    public void run() {
        /* Create and publish query */
        try {
            NetworkDestination dest = new NetworkDestination("localhost", 5555);
            QueryRequest event = new QueryRequest(query);
            pendingResponse = true;
            publishQuery(event, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Collect results */
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Get list of new result canonical names and update buffer table */
        updateTable(listener.getList());

        pendingResponse = false;

        /*
         * check that we are done receiving messages before potential
         * termination
         */
        if (canceled) {
            synchronized (this) {
                notify();
            }
        }
    }

    public void disconnect() {
        if (pendingResponse) {
            try {
                canceled = true;
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        messageRouter.shutdown();
    }

    private void publishQuery(QueryRequest event, NetworkDestination dest)
            throws IOException {
        publisher.publish(dest, event);
    }

    private void updateTable(Collection<String> list) {
        synchronized (table) {
            FixedBuffer<String> buf = table.getBuffer(qid);

            /* If there is no entry create a new one */
            if (buf == null) {
                buf = new FixedBuffer<String>();
                buf.add(list);
                table.addEntry(qid, buf);
            } else {
                buf.add(list);
                table.addEntry(qid, buf);
            }
        }
    }
}
