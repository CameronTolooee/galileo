
package galileo.fs;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import galileo.dataset.Block;
import galileo.dataset.Metadata;
import galileo.graph.FeaturePath;
import galileo.graph.MetadataGraph;
import galileo.query.Query;
import galileo.serialization.SerializationException;
import galileo.serialization.Serializer;
import galileo.util.StackTraceToString;

public class GeospatialFileSystem extends FileSystem {

    private static final Logger logger = Logger.getLogger("galileo");

    private static final String metadataStore = "metadata.graph";

    public MetadataGraph metadataGraph;

    private GeospatialPhysicalGraph physicalGraph;

    public GeospatialFileSystem(String storageDirectory)
    throws FileSystemException, IOException, SerializationException {
        initialize(storageDirectory);

        File metaFile = new File(storageDirectory + "/" + metadataStore);
        if (metaFile.exists() && metaFile.length() != 0) {
            metadataGraph = Serializer.restore(MetadataGraph.class, metaFile);
        } else {
            metadataGraph = new MetadataGraph();
        }
        physicalGraph = new GeospatialPhysicalGraph(storageDirectory);
    }

    public void storeBlock(Block block)
    throws FileSystemException, IOException {
        String physicalPath = physicalGraph.storeBlock(block);

        Metadata meta = block.getMetadata();
        FeaturePath<String> path = createPath(physicalPath, meta);

        try {
            metadataGraph.addPath(path);
        } catch (Exception e) {
            throw new FileSystemException("Error storing block: "
                    + e.getClass().getCanonicalName() + ":"
                    + System.lineSeparator() +
                    StackTraceToString.convert(e));
        }
    }

    protected FeaturePath<String> createPath(
            String physicalPath, Metadata meta) {

        FeaturePath<String> path = new FeaturePath<String>(
                physicalPath, meta.getAttributes().toArray());

        return path;
    }

    public MetadataGraph query(Query query) {
        return metadataGraph.evaluateQuery(query);
    }

    public void shutdown() {
        try {
            Serializer.persist(metadataGraph,
                    storageDirectory + "/" + metadataStore);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing persistent index file", e);
        }
    }
}
