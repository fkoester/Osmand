/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.reader.IPostListener;
import eu.lighthouselabs.obd.reader.io.ObdCommandJob;

/**
 * @author Fabian Köster <f.koester@tarent.de>
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
	private double currentFuelVolume;
	private long tourStartTimestamp;
	//private int tourStartKm;
	
	/*
	 * Dynamic values read from vehicle
	 */
	private Sample<Integer> currentEngineLoad;
	private Sample<Integer> currentEngineRpm;
	private Sample<Integer> currentVelocity;
	
	/*
	 * Dynamic derived values
	 */
	private Sample<Double> currentFuelConsumptionPerHour;
	
	private Sample<Double> tourAverageVelocity;
	private Sample<Double> tourAverageEngineLoad;
	private Sample<Double> tourAverageConsumption;
	
	public VehicleModel(FuelType fuelType, int tankCapacity) {
		
		this.fuelType = fuelType;
		this.tankCapacity = tankCapacity;
		
		this.currentEngineLoad = new Sample<Integer>(0);
		this.currentEngineRpm = new Sample<Integer>(0);
		this.currentVelocity = new Sample<Integer>(0);
		this.currentFuelConsumptionPerHour = new Sample<Double>(Double.valueOf(0));
		
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

	public double getCurrentFuelVolume() {
		return currentFuelVolume;
	}

	public void setCurrentFuelVolume(double currentFuelVolume) {
		this.currentFuelVolume = currentFuelVolume;
	}

	public int getCurrentEngineLoad() {
		
		return currentEngineLoad.getValue();
	}

	public int getCurrentEngineRpm() {
		return currentEngineRpm.getValue();
	}

	public int getCurrentVelocity() {
		
		return currentVelocity.getValue();
	}
	
	public double getCurrentFuelConsumptionPerHour() {
		
		return currentFuelConsumptionPerHour.getValue();
	}
	
	public double getCurrentFuelConsumptionPer100km() {
		
		return getCurrentFuelConsumptionPerHour() * 100 / (double)getCurrentVelocity();
	}
	
	public double getCurrentRangeDistance() {
		
		return getCurrentFuelVolume() / getCurrentFuelConsumptionPerHour() * getCurrentVelocity();
	}
	
	public double getCurrentRangeTime() {
		
		return getCurrentFuelVolume() / getCurrentFuelConsumptionPerHour();
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
		
		long timestamp = System.currentTimeMillis();

		if(AvailableCommandNames.ENGINE_LOAD.getValue().equals(cmdName)) {
			
			int engineLoad;
			
			if(job.getCommand().getBuffer().size() > 2)
				engineLoad = job.getCommand().getBuffer().get(2);
			else
				engineLoad = 0;
						
			currentEngineLoad = new Sample<Integer>(timestamp, engineLoad);
			updateAverage(tourAverageEngineLoad, currentEngineLoad);

			// TODO find real formula	
			Double consumptionPerHour = engineLoad * 0.05351558818533617;
			
			currentFuelVolume -= consumptionPerHour * ((timestamp - currentFuelConsumptionPerHour.getTimestamp()) / 1000 / 60 / 60) ;
			
			currentFuelConsumptionPerHour = new Sample<Double>(timestamp, consumptionPerHour);			
			updateAverage(tourAverageConsumption, currentFuelConsumptionPerHour);
			
		} else if (AvailableCommandNames.SPEED.getValue().equals(cmdName)) {
			int velocity;
			if(job.getCommand().getBuffer().size() > 2)
				velocity = job.getCommand().getBuffer().get(2);
			else
				velocity = 0;
			
			currentVelocity = new Sample<Integer>(timestamp, velocity);
			updateAverage(tourAverageVelocity, currentVelocity);
		}
	}
	
	private <T extends Number> void updateAverage(Sample<Double> average, Sample<T> newSample) {		
		
		average.setValue((average.getValue() * (average.getTimestamp() - tourStartTimestamp) + newSample.getValue().doubleValue() * (newSample.getTimestamp()-average.getTimestamp())) / (newSample.getTimestamp() - tourStartTimestamp));
		average.setTimestamp(newSample.getTimestamp());
	}
}
