/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

/**
 * @author fabian
 *
 */
public class VehicleModel {
	
	/*
	 * Configured static values
	 */
	private FuelType fuelType;
	private int tankCapacity;
	
	/*
	 * Dynamic memorized values
	 */
	private int currentFuelVolume;
	
	/*
	 * Dynamic values read from vehicle
	 */
	private int currentEngineLoad;
	private int currentEngineRpm;
	private int currentVelocity;
	
	public VehicleModel(FuelType fuelType, int tankCapacity) {
		
		this.fuelType = fuelType;
		this.tankCapacity = tankCapacity;
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
		return currentEngineLoad;
	}

	public void setCurrentEngineLoad(int currentEngineLoad) {
		this.currentEngineLoad = currentEngineLoad;
	}

	public int getCurrentEngineRpm() {
		return currentEngineRpm;
	}

	public void setCurrentEngineRpm(int currentEngineRpm) {
		this.currentEngineRpm = currentEngineRpm;
	}

	public int getCurrentVelocity() {
		return currentVelocity;
	}

	public void setCurrentVelocity(int currentVelocity) {
		this.currentVelocity = currentVelocity;
	}
	
	public int getCurrentFuelConsumptionPerHour() {
		
		// TODO find real formula
		return getCurrentEngineLoad() / 10;
	}
	
	public int getCurrentFuelConsumptionPer100km() {
		
		return getCurrentFuelConsumptionPerHour() / (getCurrentVelocity() * 100);
	}
	
	public int getCurrentRange() {
		
		return getCurrentFuelVolume() / getCurrentFuelConsumptionPerHour() * getCurrentVelocity();
	}
}
