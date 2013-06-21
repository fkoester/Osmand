/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import net.osmand.PlatformUtil;

import org.apache.commons.logging.Log;

import android.content.ComponentName;
import android.os.IBinder;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;
import eu.lighthouselabs.obd.reader.io.ObdGatewayServiceConnection;

/**
 * @author fabian
 *
 */
public class OsmAndObdGatewayServiceConnection extends
		ObdGatewayServiceConnection {
	
	private static final Log log = PlatformUtil.getLog(OsmAndObdGatewayServiceConnection.class);

	public OsmAndObdGatewayServiceConnection() {
		super();
		log.info("Constructed");
	}

	@Override
	public void addJobToQueue(ObdCommandJob job) {
		//log.info("addJobToQueue");
		super.addJobToQueue(job);
	}

	@Override
	public boolean isRunning() {
		//log.info("isRunning");
		return super.isRunning();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		log.info("onServiceConnected");
		super.onServiceConnected(name, binder);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		log.info("onServiceDisconnected");
		super.onServiceDisconnected(name);
	}

	@Override
	public void setServiceListener(IPostListener listener) {
		log.info("setServiceListener");
		super.setServiceListener(listener);
	}

}
