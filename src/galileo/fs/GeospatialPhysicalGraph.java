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

package galileo.fs;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import galileo.dataset.Block;
import galileo.dataset.Coordinates;
import galileo.dataset.Metadata;
import galileo.dataset.SpatialProperties;
import galileo.serialization.SerializationException;
import galileo.serialization.Serializer;
import galileo.util.GeoHash;

/**
 * Creates an on-disk physical graph for Geospatial data.  This physical graph
 * manager assumes that the information being stored has both space and time
 * properties.
 * <p>
 * Relevant system properties include
 * galileo.fs.GeospatialPhysicalGraph.timeFormat and
 * galileo.fs.GeospatialPhysicalGraph.geohashPrecision
 * to modify how the hierarchy is created.
 */
public class GeospatialPhysicalGraph {

    private static final String DEFAULT_TIME_FORMAT = "yyyy/M/d";
    private static final int DEFAULT_GEOHASH_PRECISION = 5;

    private String storageDirectory;

    private SimpleDateFormat timeFormatter;
    private String timeFormat;
    private int geohashPrecision;

    public GeospatialPhysicalGraph(String storageDirectory) {
        this.storageDirectory = storageDirectory;
        this.timeFormat = System.getProperty(
                "galileo.fs.GeospatialPhysicalGraph.timeFormat",
                DEFAULT_TIME_FORMAT);
        this.geohashPrecision = Integer.parseInt(System.getProperty(
                "galileo.fs.GeospatialPhysicalGraph.geohashPrecision",
                DEFAULT_GEOHASH_PRECISION + ""));

        timeFormatter = new SimpleDateFormat();
        timeFormatter.applyPattern(timeFormat);
    }

    public Block loadBlock(String blockPath)
    throws IOException, SerializationException {
        File blockFile = new File(blockPath);
        byte[] blockBytes = new byte[(int) blockFile.length()];
        Block block = Serializer.deserialize(Block.class, blockBytes);
        return block;
    }


    public String storeBlock(Block block)
    throws IOException {
        String name = block.getMetadata().getName();
        if (name.equals("")) {
            UUID blockUUID = UUID.nameUUIDFromBytes(block.getData());
            name = blockUUID.toString();
        }

        String blockDirPath = storageDirectory + "/"
            + getStorageDirectory(block);
        String blockPath = blockDirPath + "/" + name
            + FileSystem.BLOCK_EXTENSION;

        /* Ensure the storage directory is there. */
        File blockDirectory = new File(blockDirPath);
        if (!blockDirectory.exists()) {
            if (!blockDirectory.mkdirs()) {
                throw new IOException("Failed to create directory (" +
                    blockDirPath + ") for block.");
            }
        }

        FileOutputStream blockOutStream = new FileOutputStream(blockPath);
        byte[] blockData = Serializer.serialize(block);
        blockOutStream.write(blockData);
        blockOutStream.close();

        return blockPath;
    }

    public String getStorageDirectory(Block block) {
        String directory = "";

        Metadata meta = block.getMetadata();
        Date date = meta.getTemporalProperties().getLowerBound();

        directory = timeFormatter.format(date) + "/";

        Coordinates coords = null;
        SpatialProperties spatialProps = meta.getSpatialProperties();
        if (spatialProps.hasRange()) {
            coords = spatialProps.getSpatialRange().getCenterPoint();
        } else {
            coords = spatialProps.getCoordinates();
        }
        directory += GeoHash.encode(coords, geohashPrecision);

        return directory;
    }
}
