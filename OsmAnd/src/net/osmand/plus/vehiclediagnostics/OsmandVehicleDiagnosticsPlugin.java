/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import java.util.EnumSet;

import net.osmand.PlatformUtil;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.vehiclediagnostics.widgets.EngineLoadWidget;
import net.osmand.plus.vehiclediagnostics.widgets.EngineRpmWidget;
import net.osmand.plus.vehiclediagnostics.widgets.FuelConsumptionWidget;
import net.osmand.plus.vehiclediagnostics.widgets.TourWidget;
import net.osmand.plus.vehiclediagnostics.widgets.VehicleSpeedWidget;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;

import org.apache.commons.logging.Log;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineLoadObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import eu.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import eu.lighthouselabs.obd.commands.engine.ThrottlePositionObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelTrimObdCommand;
import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.enums.FuelTrim;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.activity.ConfigActivity;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;

/**
 * @author Fabian Köster <f.koester@tarent.de>
 *
 */
public class OsmandVehicleDiagnosticsPlugin extends OsmandPlugin {
	
	private static final String ID = "osmand.vehiclediagnostics";
	private OsmandApplication app;
	private OsmandSettings settings;
	private static final Log log = PlatformUtil.getLog(OsmandVehicleDiagnosticsPlugin.class);
	
	private IPostListener postListener = null;
	private Intent serviceIntent = null;
	private OsmAndObdGatewayServiceConnection serviceConnection = null;
	
	private Handler handler = new Handler();
	
	private VehicleSpeedWidget vehicleSpeedWidget;
	private EngineRpmWidget engineRpmWidget;
	private EngineLoadWidget engineLoadWidget;
	
	private FuelConsumptionWidget fuelConsumptionWidget;
	private TourWidget tourWidget;
	private TextInfoWidget rangeWidget;

	private double costsPerLiter = 1.509;
	
	private VehicleModel vehicleModel;
	private VehicleDataCsvLogger dataLogger;
	
	public OsmandVehicleDiagnosticsPlugin(OsmandApplication app) {
		this.app = app;
	}
	
	@Override
	public boolean init(final OsmandApplication app) {
		this.settings = app.getSettings();
		
		final BluetoothAdapter btAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (btAdapter == null) {
			log.error("No bluetooth adapter");
		} else {
			// Bluetooth device is enabled?
			if (!btAdapter.isEnabled()) {
				
				log.error("Bluetooth not enabled");
			}
		}
		
		postListener = new IPostListener() {
			
			long lastUpdate;
					
			public void stateUpdate(ObdCommandJob job) {
				
				vehicleModel.stateUpdate(job);
				dataLogger.stateUpdate(job);
				
				if(lastUpdate + 1000 <= System.currentTimeMillis()) {
					
					engineLoadWidget.refresh();
					engineRpmWidget.refresh();
					vehicleSpeedWidget.refresh();
					fuelConsumptionWidget.refresh();
					tourWidget.refresh();
					lastUpdate = System.currentTimeMillis();
				}
			}
		};
		
		vehicleModel = new VehicleModel(FuelType.SUPER_E10, 50);
		
		/*
		 * Prepare service and its connection
		 */
		serviceIntent = new Intent(app, OsmAndObdGatewayService.class);
		serviceConnection = new OsmAndObdGatewayServiceConnection();
		serviceConnection.setServiceListener(postListener);
		
		SharedPreferences prefs = PreferenceManager
		        .getDefaultSharedPreferences(app);
		
		prefs.edit().putString(ConfigActivity.BLUETOOTH_LIST_KEY, "11:22:33:AA:BB:CC");
		
		log.info("Binding service...");
		boolean bindResult = app.bindService(serviceIntent, serviceConnection,
				Context.BIND_AUTO_CREATE);
		
		if(!bindResult) {
			log.error("Service binding failed");
			return false;
		}
		
		if (!serviceConnection.isRunning()) {
			log.info("Starting service...");
			app.startService(serviceIntent);
		}
		
		dataLogger = new VehicleDataCsvLogger(app);
		
		return true;
	}

	@Override
	public String getId() {
		
		return ID;
	}

	@Override
	public String getDescription() {
		return "OBDII car interface";
	}

	@Override
	public String getName() {
		
		return "Vehicle Diagnostics";
	}

	/**
	 * @see net.osmand.plus.OsmandPlugin#registerLayers(net.osmand.plus.activities.MapActivity)
	 */
	@Override
	public void registerLayers(MapActivity activity) {
		MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
		if (mapInfoLayer != null ) {
			vehicleSpeedWidget = new VehicleSpeedWidget(activity, vehicleModel);
			mapInfoLayer.getMapInfoControls().registerSideWidget(vehicleSpeedWidget,
					R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_speed, "vehiclespeed", false,
					EnumSet.allOf(ApplicationMode.class),
					EnumSet.noneOf(ApplicationMode.class), 20);
			
			
			engineRpmWidget = new EngineRpmWidget(activity, vehicleModel);
			mapInfoLayer.getMapInfoControls().registerSideWidget(engineRpmWidget,
					R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_enginerpm, "enginerpm", false,
					EnumSet.allOf(ApplicationMode.class),
					EnumSet.noneOf(ApplicationMode.class), 21);
			
			engineLoadWidget = new EngineLoadWidget(activity, vehicleModel);
			mapInfoLayer.getMapInfoControls().registerSideWidget(engineLoadWidget,
					R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_engineload, "engineload", false,
					EnumSet.allOf(ApplicationMode.class),
					EnumSet.noneOf(ApplicationMode.class), 22);
						
			fuelConsumptionWidget = new FuelConsumptionWidget(activity, vehicleModel);
			mapInfoLayer.getMapInfoControls().registerSideWidget(fuelConsumptionWidget,
					R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_fuelconsumption, "fuelconsumption", true,
					EnumSet.allOf(ApplicationMode.class),
					EnumSet.noneOf(ApplicationMode.class), 23);
			
			tourWidget = new TourWidget(activity, vehicleModel, costsPerLiter);
			mapInfoLayer.getMapInfoControls().registerSideWidget(tourWidget,
					R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_tour, "tour", true,
					EnumSet.allOf(ApplicationMode.class),
					EnumSet.noneOf(ApplicationMode.class), 24);
			
			mapInfoLayer.recreateControls();
			
			handler.post(mQueueCommands);
		}
	}
	
//	private void setRecordListener(final TextInfoWidget vehicleDiagnosticsControl, final MapActivity mapActivity) {
//		vehicleDiagnosticsControl.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//				final Location loc = app.getLocationProvider().getLastKnownLocation();
//				
//				dataLogger.logEntry(System.currentTimeMillis(), loc, "fuelup", 100);
//			}
//		});
//	}
	
	private Runnable mQueueCommands = new Runnable() {
		public void run() {
			/*
			 * If values are not default, then we have values to calculate MPG
			 */
//			Log.d(TAG, "SPD:" + speed + ", MAF:" + maf + ", LTFT:" + ltft);
//			if (speed > 1 && maf > 1 && ltft != 0) {
//				FuelEconomyWithMAFObdCommand fuelEconCmd = new FuelEconomyWithMAFObdCommand(
//						FuelType.DIESEL, speed, maf, ltft, false /* TODO */);
//				TextView tvMpg = (TextView) findViewById(R.id.fuel_econ_text);
//				String liters100km = String.format("%.2f", fuelEconCmd.getLitersPer100Km());
//				tvMpg.setText("" + liters100km);
//				
//			}
			
			boolean dummyData = true;

			if(dummyData) {				
				postListener.stateUpdate(new ObdCommandJob(new DummyObdCommand(AvailableCommandNames.ENGINE_LOAD.getValue())));
				postListener.stateUpdate(new ObdCommandJob(new DummyObdCommand(AvailableCommandNames.SPEED.getValue())));
				
				
			} else {
				if (serviceConnection.isRunning())
					queueCommands();
			}

			// run again in 50ms
			handler.postDelayed(mQueueCommands, 50);
		}
	};
	
	private void queueCommands() {
		log.debug("Queuing obd commands...");
		
		//final ObdCommandJob airTemp = new ObdCommandJob(new AmbientAirTemperatureObdCommand());
		final ObdCommandJob speed = new ObdCommandJob(new SpeedObdCommand());
		//final ObdCommandJob fuelEcon = new ObdCommandJob(new FuelEconomyObdCommand());
		final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());
		final ObdCommandJob engineLoad = new ObdCommandJob(new EngineLoadObdCommand());
		
		//final ObdCommandJob fuelLevel = new ObdCommandJob(new FuelLevelObdCommand());
		
		//final ObdCommandJob ltft1 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1));
		final ObdCommandJob ltft2 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_2));
		//final ObdCommandJob stft1 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1));
		final ObdCommandJob stft2 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_2));
		
		//final ObdCommandJob fuelConsump = new ObdCommandJob(new FuelConsumptionObdCommand());
		final ObdCommandJob throttlePos = new ObdCommandJob(new ThrottlePositionObdCommand());
		
		final ObdCommandJob maf = new ObdCommandJob(new MassAirFlowObdCommand());


		//serviceConnection.addJobToQueue(airTemp);
		serviceConnection.addJobToQueue(speed);
		//serviceConnection.addJobToQueue(fuelEcon);
		serviceConnection.addJobToQueue(rpm);
		
		serviceConnection.addJobToQueue(engineLoad);
		//serviceConnection.addJobToQueue(fuelLevel);
		//serviceConnection.addJobToQueue(ltft2);
		//serviceConnection.addJobToQueue(ltft2);
		//serviceConnection.addJobToQueue(stft2);
		//serviceConnection.addJobToQueue(stft2);
		
		//serviceConnection.addJobToQueue(fuelConsump);
		serviceConnection.addJobToQueue(throttlePos);
		//serviceConnection.addJobToQueue(maf);
	}
}
