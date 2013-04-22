/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import java.util.LinkedList;

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
	private Sample<Double> tourAverageVelocity;
	private Sample<Double> tourAverageEngineLoad;
	private Sample<Double> tourAverageConsumption;
	
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
		
		return getCurrentFuelConsumptionPerHour() * 100 / (double)getCurrentVelocity();
	}
	
	public double getCurrentRange() {
		
		return getCurrentFuelVolume() / getCurrentFuelConsumptionPerHour() * getCurrentVelocity();
	}
	
	public double getTourTotalConsumption() {
		
		return getTourFuelConsumptionPerHour() * ((double)getTourDuration() / (double)(1000 * 60 * 60));
	}
	
	public long getTourDuration() {
		
		return System.currentTimeMillis() - tourStartTimestamp;
	}
	
	public double getTourDistance() {
		
		return tourAverageVelocity.getValue() * ((double)getTourDuration() / (double)(1000 * 60 * 60));
	}
	
	public double getTourFuelConsumptionPer100km() {
		
		return getTourFuelConsumptionPerHour() * 100 / (double)tourAverageVelocity.getValue();
	}
	
	public double getTourFuelConsumptionPerHour() {
		
		return tourAverageConsumption.getValue();
	}
	
	public void resetTour() {
		
		tourStartTimestamp = System.currentTimeMillis();
		//tourStartKm = 0;
		tourAverageEngineLoad = new Sample<Double>(tourStartTimestamp, Double.valueOf(0));
		tourAverageVelocity = new Sample<Double>(tourStartTimestamp, Double.valueOf(0));
		tourAverageConsumption = new Sample<Double>(tourStartTimestamp, Double.valueOf(0));
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
			
			updateAverage(tourAverageEngineLoad, engineLoad, engineLoadHistory);

			// TODO find real formula	
			Double consumptionPerHour = engineLoad * 0.05351558818533617;
			updateAverage(tourAverageConsumption, consumptionPerHour, consumptionHistory);
			
		} else if (AvailableCommandNames.SPEED.getValue().equals(cmdName)) {
			int velocity;
			if(job.getCommand().getBuffer().size() > 2)
				velocity = job.getCommand().getBuffer().get(2);
			else
				velocity = 0;
			
			updateAverage(tourAverageVelocity, velocity, velocityHistory);
		}
	}
	
	private <T extends Number> void updateAverage(Sample<Double> average, T newValue, LinkedList<Sample<T>> history) {
		
		long timestamp = System.currentTimeMillis();
		
		if(history.isEmpty()) {
			average.setValue(newValue.doubleValue());
		}
		else {
			average.setValue((average.getValue() * (history.getLast().getTimestamp() - tourStartTimestamp) + newValue.doubleValue() * (timestamp-history.getLast().getTimestamp())) / (timestamp - tourStartTimestamp));
		}
		
		average.setTimestamp(timestamp);
		history.add(new Sample<T>(timestamp, newValue));
	}
}
