package galileo.sampling;

import java.util.ArrayList;

public interface GalileoSampleInterface <T> {
	public DataArray Sample(ArrayList<T> response);
}
