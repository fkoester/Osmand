/**
 * 
 */
package net.osmand.plus.vehiclediagnostics.widgets;

import android.view.View;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.vehiclediagnostics.VehicleModel;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;

/**
 * @author fabian
 * 
 */
public class TourWidget extends TextInfoWidget {
	
	private final static int CONSUMPTION_PER_HOUR = 0;
	private final static int CONSUMPTION_PER_100KM = 1;
	private final static int CONSUMPTION = 2;
	private final static int COSTS = 3;
	private final static int DURATION = 4;
	private final static int DISTANCE = 5;
	

	private int mode = CONSUMPTION;
	private VehicleModel vehicleModel;
	
	private double costsPerLiter;

	public TourWidget(MapActivity activity, VehicleModel vehicleModel, double costsPerLiter) {

		super(activity, 0, activity.getMapLayers().getMapInfoLayer()
				.getPaintText(), activity.getMapLayers().getMapInfoLayer()
				.getPaintSubText());
		this.vehicleModel = vehicleModel;
		this.costsPerLiter = costsPerLiter;

		setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				mode = (mode + 1) % 6;
				valueChanged();
			}
		});
		valueChanged();
	}

	public void valueChanged() {

		double value;
		String suffix;

		switch (mode) {
		case CONSUMPTION:
			value = vehicleModel.getTourTotalConsumption();
			suffix = "l";
			break;
		case COSTS:
			value = vehicleModel.getTourTotalConsumption() * costsPerLiter;
			suffix = "€";
			break;
		case DURATION:
			value = vehicleModel.getTourDuration() / 1000 / 60;
			suffix = "min";
			break;
		case DISTANCE:
			value = vehicleModel.getTourDistance();
			suffix = "km";
			break;
		case CONSUMPTION_PER_100KM:
			double fuelConsumptionPer100km = vehicleModel
					.getTourFuelConsumptionPer100km();

			if (!Double.isInfinite(fuelConsumptionPer100km)) {
				value = fuelConsumptionPer100km;
				suffix = "l/100km";
				break;
			}
		case CONSUMPTION_PER_HOUR:
		default:
			value = vehicleModel.getTourFuelConsumptionPerHour();
			suffix = "l/h";
		}

		setText(String.format("%.2f%n", value), suffix);
	}
}
