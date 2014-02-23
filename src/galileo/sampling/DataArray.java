package galileo.sampling;

import galileo.dataset.SpatialRangeImpl;
import galileo.serialization.ByteSerializable;
import galileo.serialization.SerializableArray;
import galileo.serialization.SerializationInputStream;

import java.io.IOException;

public class DataArray extends SerializableArray<ByteSerializable> {
	private static final long serialVersionUID = 8787327315762435003L;
	
	public DataArray() {}
	
	public DataArray(SerializationInputStream in) throws IOException{
		int size =  in.readInt();
		for(int i = 0; i < size; ++i){
			add(new SpatialRangeImpl(in));
		}
	}
}
