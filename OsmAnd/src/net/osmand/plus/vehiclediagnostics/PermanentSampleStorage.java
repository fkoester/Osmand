/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.osmand.plus.OsmandApplication;

/**
 * @author Fabian Köster <f.koester@tarent.de>
 *
 */
public class PermanentSampleStorage<T> {
	
	private File file;
	private Properties properties;

	public PermanentSampleStorage(final OsmandApplication app, String name) {
		
		File directory = app.getAppPath("vehicledata/");
		
		file = new File(directory, name + ".properties");
		properties = new Properties();
		
		try {
			if(file.exists())
				properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean hasSample(String key) {
		
		return properties.containsKey(key);
	}
	
	public Sample<Double> getSample(String key) {
		
		Object value = properties.get(key);
		
		if(value == null)
			return null;
		
		String asString = value.toString();
		
		if(!asString.contains(":"))
			return null;
		
		String[] parts = asString.split(":");
		
		if(parts.length != 2)
			return null;
			
		long timestamp = Long.valueOf(parts[0]);
		
		return new Sample<Double>(timestamp, Double.valueOf(parts[1]));
	}
	
	public void setSample(String key, Sample<Double> sample) {
		
		properties.setProperty(key, String.valueOf(sample.getTimestamp()) + ":" + String.valueOf(sample.getValue()));
	}
	
	public void write() {
		
		try {
			properties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
