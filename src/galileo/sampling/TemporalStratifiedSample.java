package galileo.sampling;

import galileo.dataset.BlockMetadata;

import java.util.ArrayList;
import java.util.Random;

public class TemporalStratifiedSample<T> implements GalileoSampleInterface<T>{

	public TemporalStratifiedSample(){}
	
	@Override
	public DataArray Sample(ArrayList<T> response) {
		Random rand = new Random(System.currentTimeMillis());
		DataArray result = new DataArray();
		// Scan response to develop stratification bins
		// For each leaf, extract temporalRange and create histogram to designate bins 
		// Sample each bin randomly
		// Group results
		for (T meta : response) {
			BlockMetadata data = null;
			if(meta instanceof BlockMetadata){
				data = (BlockMetadata) meta;
			}
			
        }
		return result;
	}	

}
