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

package galileo.dataset;

import java.io.IOException;

import galileo.serialization.ByteSerializable;
import galileo.serialization.SerializationInputStream;
import galileo.serialization.SerializationOutputStream;

public class SpatialRange implements ByteSerializable {
    private float upperLat;
    private float lowerLat;
    private float upperLon;
    private float lowerLon;

    private boolean hasElevation;
    private float upperElevation;
    private float lowerElevation;

    public SpatialRange(float lowerLat, float upperLat,
            float lowerLon, float upperLon) {
        this.lowerLat = lowerLat;
        this.upperLat = upperLat;
        this.lowerLon = lowerLon;
        this.upperLon = upperLon;

        hasElevation = false;
    }

    public SpatialRange(float lowerLat, float upperLat,
            float lowerLon, float upperLon,
            float upperElevation, float lowerElevation) {
        this.lowerLat = lowerLat;
        this.upperLat = upperLat;
        this.lowerLon = lowerLon;
        this.upperLon = upperLon;

        hasElevation = true;
        this.upperElevation = upperElevation;
        this.lowerElevation = lowerElevation;
    }

    public float getUpperBoundForLatitude() {
        return upperLat;
    }

    public float getLowerBoundForLatitude() {
        return lowerLat;
    }

    public float getUpperBoundForLongitude() {
        return upperLon;
    }

    public float getLowerBoundForLongitude() {
        return lowerLon;
    }

    public Coordinates getCenterPoint() {
        float latDifference = upperLat - lowerLat;
        float latDistance = latDifference / 2;

        float lonDifference = upperLon - lowerLon;
        float lonDistance = lonDifference / 2;

        return new Coordinates(lowerLat + latDistance,
                               lowerLon + lonDistance);
    }

    public boolean hasElevationBounds() {
        return hasElevation;
    }

    public float getUpperBoundForElevation() {
        return upperElevation;
    }

    public float getLowerBoundForElevation() {
        return lowerElevation;
    }

    @Override
    public String toString() {
        return "(" + lowerLat + ", " + lowerLon
            + "), (" + upperLat + ", " + upperLon + ")";
    }

    @Deserialize
    public SpatialRange(SerializationInputStream in)
    throws IOException {
        lowerLat = in.readFloat();
        upperLat = in.readFloat();
        lowerLon = in.readFloat();
        upperLon = in.readFloat();

        hasElevation = in.readBoolean();
        if (hasElevation) {
            lowerElevation = in.readFloat();
            upperElevation = in.readFloat();
        }
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeFloat(lowerLat);
        out.writeFloat(upperLat);
        out.writeFloat(lowerLon);
        out.writeFloat(upperLon);

        out.writeBoolean(hasElevation);
        if (hasElevation) {
            out.writeFloat(lowerElevation);
            out.writeFloat(upperElevation);
        }
    }
}
