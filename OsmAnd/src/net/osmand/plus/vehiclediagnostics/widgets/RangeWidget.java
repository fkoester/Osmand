/**
 * 
 */
package net.osmand.plus.vehiclediagnostics.widgets;

import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.vehiclediagnostics.VehicleModel;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;
import android.view.View;

/**
 * @author Fabian Köster <f.koester@tarent.de>
 *
 */
public class RangeWidget extends TextInfoWidget {
	
	private final static int FUEL_VOLUME = 0;
	private final static int RANGE_TIME = 1;
	private final static int RANGE_DISTANCE = 2;
	
	private int mode = FUEL_VOLUME;
	private VehicleModel vehicleModel;

	public RangeWidget(MapActivity activity, VehicleModel vehicleModel) {
		
		super(activity, 0, activity.getMapLayers().getMapInfoLayer()
				.getPaintText(), activity.getMapLayers().getMapInfoLayer()
				.getPaintSubText());
		this.vehicleModel = vehicleModel;

		setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				mode = (mode + 1) % 3;
				refresh();
			}
		});
		refresh();
	}
	
	public void refresh() {

		double value;
		String suffix;

		switch (mode) {
		case FUEL_VOLUME:
			value = vehicleModel.getCurrentFuelVolume();
			suffix = "l";
			break;
		case RANGE_TIME:
			value = vehicleModel.getCurrentRangeTime();
			suffix = "h";
			break;
		case RANGE_DISTANCE:
		default:
			value = vehicleModel.getCurrentRangeDistance();
			suffix = "km";
			break;
		}

		setText(String.format("%.2f%n", value), suffix);
	}
}
