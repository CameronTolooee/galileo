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

import galileo.comm.CachedQueryEvent;
import galileo.comm.ElectionResponse;
import galileo.dht.NodeInfo;
import galileo.graph.FeaturePath;
import galileo.graph.FeatureTypeMismatchException;
import galileo.graph.GraphException;
import galileo.graph.MetadataGraph;
import galileo.net.ServerMessageRouter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Maintains cached queries by initiating requires, storing intermediate data,
 * and responding to invoked cached queries.
 * 
 * @author Cameron Tolooee
 * 
 */
public class CachedQueryTracker {

    /* Maintains the cached data for each cached query */
    private CachedBufferTable table;

    /* Thread pool to execute threads at specified interval */
    private ScheduledExecutorService executor;

    /* In case of overflow, nodes to forward buffers */
    private Collection<ElectionResponse> groupNodes;
    private static final Logger logger = Logger.getLogger("galileo");

    public CachedQueryTracker() {
        table = new CachedBufferTable();
        executor = new ScheduledThreadPoolExecutor(10);
        groupNodes = new ArrayList<ElectionResponse>();
    }

    /**
     * Submit the cached query to the thread pool. At each interval step, the
     * query will be re-issued and results maintained in a
     * <code>CachedBufferTable</code> After expiration time from submission, the
     * thread will stop execution.
     * 
     * @param query
     *            The cached query to be executed
     * @param expiration
     *            Specifies how long the query should be re-evaluated in seconds
     * @param interval
     *            Specifies the time between each query re-evaluation in seconds
     * @throws IOException
     */
    public void sumbitQuery(final CachedQueryEvent cqe,
            ServerMessageRouter messageRouter) throws IOException {
        // TODO: Better exception handling
        int interval = cqe.getInterval();
        long expiration = cqe.getExpiration();

        /* create a CachedQueryWorker from the query event */
        final CachedQueryWorker worker = new CachedQueryWorker(cqe.getQuery(),
                cqe.getID(), table);

        /* submit worker that re-queries and updates table at "interval" periods */
        final ScheduledFuture<?> handle = executor.scheduleAtFixedRate(worker,
                0, interval, TimeUnit.SECONDS);

        /* After "expiration" seconds kill the worker (but let it finish) */
        executor.schedule(new Runnable() {

            public void run() {
                logger.info("Terminating continuous query: " + cqe.getID());
                worker.disconnect();
                handle.cancel(false);

                /* TODO REMOVE ME -- here for debugging purposes */
                System.out.println(table);
            }
        }, expiration, TimeUnit.SECONDS);
    }

    /**
     * Finds the entry corresponding to the provided query ID in
     * <code>CachedBufferTable</code> and reconstructs a
     * <code>MetadataGraph</code> from the buffer. If the buffer is empty or no
     * entry matches the qid, returns <code>null</code>.
     * 
     * @param qid
     *            The query identification string
     * @return <code>MetadataGraph</code> of the cached buffer corresponding to
     *         the qid
     * @throws GraphException
     * @throws FeatureTypeMismatchException
     */
    public MetadataGraph getMetadata(String qid)
            throws FeatureTypeMismatchException, GraphException {
        /* Get table entry */
        FixedBuffer<String> buffer = table.getBuffer(qid);

        // TODO
        /* Check for buffer overflow */
        /* if so, get the overflowed data */

        /* Create a metadata graph Path from each buffer entry from each buffer */
        MetadataGraph result = new MetadataGraph();
        for (String name : buffer) {
            result.addPath(new FeaturePath<String>(name));
        }

        /* Combine all graphs */
        // TODO

        return result;
    }

    private NodeInfo electNode() {
        NodeInfo highestRank = null;
        long highest = -1;
        for (ElectionResponse response : groupNodes) {
            long free = response.freeMemory();
            long total = response.totalMemory();
            if (free > highest) {
                highest = free;
                highestRank = response.getNode();
            }
        }
        return highestRank;
    }

    public Set<Entry<String, FixedBuffer<String>>> getTableEntries() {
        CachedBufferTable result = null;
        /* make sure we have a stable copy */
        synchronized (table) {
            result = table;
        }
        return result.entrySet();
    }

    public void updateGroupInfo(ElectionResponse response) {
        groupNodes.add(response);
    }

}
