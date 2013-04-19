/**
 * 
 */
package net.osmand.plus.vehiclediagnostics.widgets;

import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.vehiclediagnostics.VehicleModel;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;
import android.view.View;

/**
 * @author fabian
 *
 */
public class FuelConsumptionWidget extends TextInfoWidget {
	
	private final static int CONSUMPTION_PER_HOUR = 0;
	private final static int CONSUMPTION_PER_100KM = 1 ;
	
	private int mode = CONSUMPTION_PER_HOUR;
	private VehicleModel vehicleModel;

	public FuelConsumptionWidget(MapActivity activity, VehicleModel vehicleModel) {
	
		super(activity, 0, activity.getMapLayers().getMapInfoLayer().getPaintText(), activity.getMapLayers().getMapInfoLayer().getPaintSubText());
		this.vehicleModel = vehicleModel;
		
		setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mode = (mode+1) % 2;
				valueChanged();
			}
		});
		valueChanged();
	}
	
	public void valueChanged() {
		
		double value = mode == CONSUMPTION_PER_HOUR ? vehicleModel.getCurrentFuelConsumptionPerHour() : vehicleModel.getCurrentFuelConsumptionPer100km();
		float rounded = Math.round(value * 100) / 100;
		
		String suffix = mode == CONSUMPTION_PER_HOUR ? "l/h" : "l/100km";
		
		setText(String.valueOf(rounded), suffix);
	}
}
