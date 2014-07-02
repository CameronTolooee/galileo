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

package galileo.test.cache;

import galileo.client.EventPublisher;
import galileo.comm.CacheRecall;
import galileo.comm.CachedQueryEvent;
import galileo.comm.Disconnection;
import galileo.comm.QueryRequest;
import galileo.comm.QueryResponse;
import galileo.comm.StorageRequest;
import galileo.dataset.Block;
import galileo.dataset.Metadata;
import galileo.dataset.SpatialProperties;
import galileo.dataset.TemporalProperties;
import galileo.dataset.feature.Feature;
import galileo.dataset.feature.FeatureSet;
import galileo.event.EventContainer;
import galileo.event.EventType;
import galileo.net.ClientMessageRouter;
import galileo.net.GalileoMessage;
import galileo.net.MessageListener;
import galileo.net.NetworkDestination;
import galileo.query.Expression;
import galileo.query.Operation;
import galileo.query.Query;
import galileo.serialization.Serializer;
import galileo.util.GeoHash;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Random;

public class CacheQueryTest implements MessageListener {

    private static Random randomGenerator = new Random(System.nanoTime());

    private ClientMessageRouter messageRouter;
    private EventPublisher publisher;
    public long timer;
    public CacheQueryTest() throws IOException {
        messageRouter = new ClientMessageRouter();
        publisher = new EventPublisher(messageRouter);

        messageRouter.addListener(this);
    }

    public NetworkDestination connect(String hostname, int port)
            throws UnknownHostException, IOException {
        return messageRouter.connectTo(hostname, port);
    }

    public void disconnect() {
        messageRouter.shutdown();
    }

    @Override
    public void onMessage(GalileoMessage message) {
        if (message == null) {
            /* Connection was terminated */
            messageRouter.shutdown();
            return;
        }

        try {
            EventContainer container = Serializer.deserialize(
                    EventContainer.class, message.getPayload());

            if (container.getEventType() == EventType.QUERY_RESPONSE) {
                QueryResponse response = Serializer.deserialize(
                        QueryResponse.class, container.getEventPayload());
                /* just print out the graph to check accuracy */
                System.out.println(System.nanoTime()-timer);
                System.out.println("size: "+response.getMetadata().numVertices());
            }

            if (container.getEventType() == EventType.DISCONNECT) {
                Disconnection disconnect = Serializer.deserialize(
                        Disconnection.class, container.getEventPayload());

                System.out.println("Disconnected from " + disconnect);
            }

        } catch (Exception e) {
            System.out.println("Could not read event container");
        }
    }

    public void store(NetworkDestination destination, Block fb)
            throws Exception {
        StorageRequest store = new StorageRequest(fb);
        publisher.publish(destination, store);
    }

    public void query(NetworkDestination destination, CachedQueryEvent query)
            throws IOException {
        publisher.publish(destination, query);
    }
    
    public void query(NetworkDestination destination, Query query)
            throws IOException {
        QueryRequest qr = new QueryRequest(query);
        publisher.publish(destination, qr);
    }

    public void recall(CacheRecall recall, NetworkDestination destination)
            throws IOException {
        publisher.publish(destination, recall);
    }

    public static int randomInt(int start, int end) {
        return randomGenerator.nextInt(end - start + 1) + start;
    }

    public static float randomFloat() {
        return randomGenerator.nextFloat();
    }

    private Block generateData() {
        /* First, a temporal range for this data "sample" */
        Calendar calendar = Calendar.getInstance();
        int year, month, day;

        year = randomInt(2010, 2013);
        month = randomInt(0, 11);
        day = randomInt(1, 28);

        calendar.set(year, month, day);

        /*
         * Convert the random values to a start time, then add 1ms for the end
         * time. This simulates 1ms worth of data.
         */
        long startTime = calendar.getTimeInMillis();
        long endTime = startTime + 1;

        TemporalProperties temporalProperties = new TemporalProperties(
                startTime, endTime);

        /* The continental US */
        String[] geoRand = { "c2", "c8", "cb", "f0", "f2", "9r", "9x", "9z",
                "dp", "dr", "9q", "9w", "9y", "dn", "dq", "9m", "9t", "9v",
                "dj" };

        String geoPre = geoRand[randomInt(0, geoRand.length - 1)];
        String hash = geoPre;

        for (int i = 0; i < 10; ++i) {
            int random = randomInt(0, GeoHash.charMap.length - 1);
            hash += GeoHash.charMap[random];
        }

        SpatialProperties spatialProperties = new SpatialProperties(
                GeoHash.decodeHash(hash));

        String[] featSet = { "wind_speed", "wind_direction", "condensation",
                "temperature", "humidity" };

        FeatureSet features = new FeatureSet();
        for (int i = 0; i < 5; ++i) {
            String featureName = featSet[randomInt(0, featSet.length - 1)];
            features.put(new Feature(featureName, randomFloat() * 100));
        }

        /* Add a hard-coded constant to make querying easy */
        features.put(new Feature("test", 50f));

        Metadata metadata = new Metadata();
        metadata.setTemporalProperties(temporalProperties);
        metadata.setSpatialProperties(spatialProperties);
        metadata.setAttributes(features);

        /* Now let's make some "data" to associate with our metadata. */
        Random r = new Random(System.nanoTime());
        byte[] blockData = new byte[8000];
        r.nextBytes(blockData);

        Block b = new Block(metadata, blockData);
        return b;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: galileo.client.CacheQueryTest "
                    + "<server-hostname> <server-port> ");
            return;
        }

        String serverHostName = args[0];
        int serverPort = Integer.parseInt(args[1]);
        CacheQueryTest client = new CacheQueryTest();
        NetworkDestination server = client.connect(serverHostName, serverPort);

        /* Generate random block of data and store it */
        Block block = client.generateData();
        //client.store(server, block);

        /* Create query to be cached (on hard-coded value test=50) */
        Query query = new Query();
        query.addOperation(new Operation(new Expression("!=", new Feature(
                "temperature_surface", 0f))));
       // CachedQueryEvent cqe = new CachedQueryEvent("ID_1", 10, 3, query);
        
        
        client.timer = System.nanoTime();
        client.query(server,query);
        /* publish cached query (expires after 10 seconds) */
       // client.query(server, cqe);
        
        /* let the cache query do something */
       // Thread.sleep(5000);

        /* Recall the cached data */
       // client.timer = System.nanoTime();
       // CacheRecall recall = new CacheRecall("ID_1");
       // client.recall(recall, server);
       // Thread.sleep(5000);
       // client.disconnect();
    }
}
