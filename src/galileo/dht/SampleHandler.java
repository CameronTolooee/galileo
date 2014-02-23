package galileo.dht;

import galileo.comm.QueryResponse;
import galileo.comm.Sample;
import galileo.comm.SampleResponse;
import galileo.config.SystemConfig;
import galileo.fs.FileSystem;
import galileo.sampling.DataArray;
import galileo.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Cameron Tolooee
 * Handles a sample query from the client. Write the (temporary) jar file to root of FS. 
 * Dynamically load the user formatted jar to class path and use it to perform sampling
 * on the QueryResponse metadata graph. Publish results.
 */
public class SampleHandler extends EventHandler {

	private FileSystem fs;
	
	public SampleHandler(FileSystem fs){
		this.fs = fs;
	}
	
	@Override
	public void handleEvent() throws Exception {
		Sample sample = Serializer.deserialize(Sample.class,
				eventContainer.getEventPayload());
		String[] args = sample.getArgs();
		fs.writeJar(sample.getJarBytes(), SystemConfig.getStorageRoot()+"/"+args[0]); // First argument is the jar file name 
																// Second is the name of the class
		File myJar = new File(SystemConfig.getStorageRoot()+"/"+args[0]);
		URL[] jar = {myJar.toURI().toURL()};
		URLClassLoader child = new URLClassLoader (jar, this.getClass().getClassLoader());
		Class<?> classToLoad = Class.forName (args[1], true, child);
		Constructor<?> ctor = classToLoad.getConstructor(new Class[]{String.class, String.class});
		Object sampleInstance = ctor.newInstance(args[2], args[3]);
		Method method = sampleInstance.getClass().getMethod("Sample", QueryResponse.class);
		Object result = method.invoke(sampleInstance, sample.getMetaData());
		if(result instanceof DataArray){
			SampleResponse response = new SampleResponse((DataArray) result);
			publishResponse(response);
			if(!myJar.delete()){
				throw new Exception("Delete operation failed: unable to remove "+ myJar.getName());
			}
		} else {
			throw new Exception("Invalid sample jar file return type.");
		}

		  // TODO remove jar file after computation
	}
	
    /**
     * Parameters of the method to add an URL to the System classes. 
     */
    private static final Class<?>[] parameters = new Class[]{URL.class};

    /**
     * Adds a file to the classpath.
     * @param s a String pointing to the file
     * @throws IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Adds a file to the classpath
     * @param f the file to be added
     * @throws IOException
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    /**
     * Adds the content pointed by the URL to the classpath.
     * @param u the URL pointing to the content to be added
     * @throws IOException
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u }); 
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }      
    }


}
