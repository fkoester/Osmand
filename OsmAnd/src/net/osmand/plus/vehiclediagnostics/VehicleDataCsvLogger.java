/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.osmand.Location;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;

import org.apache.commons.logging.Log;

import au.com.bytecode.opencsv.CSVWriter;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;

/**
 * @author Fabian Köster <f.koester@tarent.de>
 *
 */
public class VehicleDataCsvLogger implements IPostListener {
	
	private CSVWriter csvWriter;
	
	private OsmandApplication app;
	
	private static final Log log = PlatformUtil.getLog(VehicleDataCsvLogger.class);

	public VehicleDataCsvLogger(final OsmandApplication app) {
		
		this.app = app;
		
		try {
			File directory = app.getAppPath("vehicledata/");
			directory.mkdirs();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
			File csvFile = new File(directory, dateFormat.format(new Date()) + ".csv");
			csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(csvFile)));
			log.info("Successfully opened CSV file at "+ csvFile);
		} catch (IOException e) {
			log.error("Could not access CSV file.", e);
		}
	}
	
	public void logEntry(long timestamp, Location location, String name, Object value) {
		
		String[] entries = new String[] {
				String.valueOf(timestamp),
				location != null ? String.valueOf(location.getLatitude()) : "",
				location != null ? String.valueOf(location.getLongitude()) : "",
				name,
				value.toString() };
	    csvWriter.writeNext(entries);
	    try {
			csvWriter.flush();
			log.info("Flushed csv file");
		} catch (IOException e) {
			log.error("Could not flush csv file", e);
		}
	}

	/**
	 * @see eu.lighthouselabs.obd.reader.IPostListener#stateUpdate(eu.lighthouselabs.obd.reader.io.ObdCommandJob)
	 */
	@Override
	public void stateUpdate(ObdCommandJob job) {
		
		String cmdName = job.getCommand().getName();
		String cmdResult = job.getCommand().getResult();
		
		final Location loc = app.getLocationProvider().getLastKnownLocation();
		
		String[] entries = new String[] {
				String.valueOf(System.currentTimeMillis()),
				loc != null ? String.valueOf(loc.getLatitude()) : "NOLOC",
				loc != null ? String.valueOf(loc.getLongitude()) : "NOLOC",
				cmdName != null ? cmdName : "UNKNOWN",
				cmdResult != null ? cmdResult : "NORESULT"};
		
	    csvWriter.writeNext(entries);
	}
}
