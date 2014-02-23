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

package galileo.bmp;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;

/**
 * Provides functionality for visualizing bitmaps.
 *
 * @author malensek
 */
public class Visualization {

    /**
     * Given an iterator of integer indices in a bit set, this method will draw
     * the visual representation of the set.  This is useful for visualizing
     * what a bitmap index or query polygon looks like.
     *
     * @param it Iterator of bit set indices.
     * @param width Width of the output image.
     * @param height Height of the output image.
     *
     * @return A BufferedImage with the visual representation of the bitset.
     */
    public static BufferedImage drawIterableMap(Iterator<Integer> it,
            int width, int height) {

        return drawIterableMap(it, width, height, Color.WHITE);
    }

    /**
     * Given an iterator of integer indices in a bit set, this method will draw
     * the visual representation of the set.  This is useful for visualizing
     * what a bitmap index or query polygon looks like.
     *
     * @param it Iterator of bit set indices.
     * @param width Width of the output image.
     * @param height Height of the output image.
     * @param color Color of bits set to 1.
     *
     * @return A BufferedImage with the visual representation of the bitset.
     */
    public static BufferedImage drawIterableMap(Iterator<Integer> it,
            int width, int height, Color color) {

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        while (it.hasNext()) {
            int index = it.next();
            int x = index % width;
            int y = index / width;
            img.setRGB(x, y, color.getRGB());
        }

        return img;
    }
}
