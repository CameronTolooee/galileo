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

package galileo.sampling;

import galileo.dataset.BlockMetadata;

import java.util.ArrayList;
import java.util.Random;

public class SimpleRandomSample<T> implements GalileoSampleInterface<T> {
	private double ratio;
	
	public SimpleRandomSample (String feature, String ratio) {
		this.ratio = Double.parseDouble(ratio);
	}

	
	// TODO: break dependency on Galileo types
	public DataArray Sample(ArrayList<T> response) {
		Random rand = new Random(System.currentTimeMillis());
		DataArray result = new DataArray();
		for (T meta : response) {
			if(rand.nextDouble() < ratio) {
				result.add( ((BlockMetadata) meta));
			}
        }
		return result;
	}	
}
