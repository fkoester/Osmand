/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import java.util.LinkedList;

import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;

/**
 * @author fabian
 *
 */
public class VehicleModel implements IPostListener {
	
	/*
	 * Configured static values
	 */
	private FuelType fuelType;
	private int tankCapacity;
	
	/*
	 * Dynamic memorized values
	 */
	private int currentFuelVolume;
	private long tourStartTimestamp;
	//private int tourStartKm;
	
	/*
	 * Dynamic values read from vehicle
	 */
	//private int currentEngineLoad;
	private int currentEngineRpm;
	//private int currentVelocity;
	
	/*
	 * Dynamic derived values
	 */
	private double tourAverageVelocity;
	private double tourAverageEngineLoad;
	private double tourAverageConsumption;
	
	/*
	 * Storage of sample histories
	 */
	private LinkedList<Sample<Integer>> velocityHistory;
	private LinkedList<Sample<Integer>> engineLoadHistory;
	private LinkedList<Sample<Double>> consumptionHistory;
	
	public VehicleModel(FuelType fuelType, int tankCapacity) {
		
		this.fuelType = fuelType;
		this.tankCapacity = tankCapacity;
		
		velocityHistory = new LinkedList<Sample<Integer>>();
		engineLoadHistory = new LinkedList<Sample<Integer>>();
		consumptionHistory = new LinkedList<Sample<Double>>();
		
		resetTour();
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public int getTankCapacity() {
		return tankCapacity;
	}

	public void setTankCapacity(int tankCapacity) {
		this.tankCapacity = tankCapacity;
	}

	public int getCurrentFuelVolume() {
		return currentFuelVolume;
	}

	public void setCurrentFuelVolume(int currentFuelVolume) {
		this.currentFuelVolume = currentFuelVolume;
	}

	public int getCurrentEngineLoad() {
		if(!engineLoadHistory.isEmpty())
			return engineLoadHistory.getLast().getValue();
		
		return 0;
	}

	public int getCurrentEngineRpm() {
		return currentEngineRpm;
	}

	public void setCurrentEngineRpm(int currentEngineRpm) {
		this.currentEngineRpm = currentEngineRpm;
	}

	public int getCurrentVelocity() {
		if(!velocityHistory.isEmpty())
			return velocityHistory.getLast().getValue();
		
		return 0;
	}
	
	public double getCurrentFuelConsumptionPerHour() {
		
		if(!consumptionHistory.isEmpty())
			return consumptionHistory.getLast().getValue();
		
		return 0;
	}
	
	public double getCurrentFuelConsumptionPer100km() {
		
		return getCurrentFuelConsumptionPerHour() / ((double)getCurrentVelocity() * 100);
	}
	
	public double getCurrentRange() {
		
		return getCurrentFuelVolume() / getCurrentFuelConsumptionPerHour() * getCurrentVelocity();
	}
	
	public double getTourTotalConsumption() {
		
		return getTourFuelConsumptionPerHour() * (getTourDuration() * 1000 * 60 * 60);
	}
	
	public long getTourDuration() {
		
		return System.currentTimeMillis() - tourStartTimestamp;
	}
	
	public double getTourDistance() {
		
		return tourAverageVelocity * (getTourDuration() * 1000 * 60 * 60);
	}
	
	public double getTourFuelConsumptionPer100km() {
		
		return getTourFuelConsumptionPerHour() / ((double)tourAverageVelocity * 100);
	}
	
	public double getTourFuelConsumptionPerHour() {
		
		return tourAverageConsumption;
	}
	
	public void resetTour() {
		
		tourStartTimestamp = System.currentTimeMillis();
		//tourStartKm = 0;
		tourAverageEngineLoad = 0;
		tourAverageVelocity = 0;
	}

	/**
	 * @see eu.lighthouselabs.obd.reader.IPostListener#stateUpdate(eu.lighthouselabs.obd.reader.io.ObdCommandJob)
	 */
	@Override
	public void stateUpdate(ObdCommandJob job) {
		
		String cmdName = job.getCommand().getName();

		if(AvailableCommandNames.ENGINE_LOAD.getValue().equals(cmdName)) {
			
			int engineLoad;
			
			if(job.getCommand().getBuffer().size() > 2)
				engineLoad = job.getCommand().getBuffer().get(2);
			else
				engineLoad = 0;
			
			long timestamp = System.currentTimeMillis();
			
			if(engineLoadHistory.isEmpty()) {
				tourAverageEngineLoad = engineLoad;
			}
			else {
				tourAverageEngineLoad = (tourAverageEngineLoad * (engineLoadHistory.getLast().getTimestamp() - tourStartTimestamp) + engineLoad * (timestamp-engineLoadHistory.getLast().getTimestamp())) / (timestamp - System.currentTimeMillis());
			}
			
			engineLoadHistory.add(new Sample<Integer>(engineLoad));
			
			// TODO find real formula	
			double consumptionPerHour = engineLoad * 0.05351558818533617;
			
			if(consumptionHistory.isEmpty()) {
				tourAverageConsumption = consumptionPerHour;
			}
			else {
				tourAverageConsumption = (tourAverageConsumption * (consumptionHistory.getLast().getTimestamp() - tourStartTimestamp) + consumptionPerHour * (timestamp-consumptionHistory.getLast().getTimestamp())) / (timestamp - System.currentTimeMillis());
			}
			
			consumptionHistory.add(new Sample<Double>(consumptionPerHour));
			
		} else if (AvailableCommandNames.SPEED.getValue().equals(cmdName)) {
			int velocity = ((SpeedObdCommand)job.getCommand()).getMetricSpeed();
			long timestamp = System.currentTimeMillis();
				
			if(velocityHistory.isEmpty()) {
				tourAverageVelocity = velocity;
			}
			else {
				tourAverageVelocity = (tourAverageVelocity * (velocityHistory.getLast().getTimestamp() - tourStartTimestamp) + velocity * (timestamp-velocityHistory.getLast().getTimestamp())) / (timestamp - System.currentTimeMillis());
			}
			
			velocityHistory.add(new Sample<Integer>(velocity));
		}
	}
}
