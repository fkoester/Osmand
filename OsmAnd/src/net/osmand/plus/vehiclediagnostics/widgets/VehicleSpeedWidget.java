/**
 * 
 */
package net.osmand.plus.vehiclediagnostics.widgets;

import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;

/**
 * @author fabian
 *
 */
public class VehicleSpeedWidget extends TextInfoWidget {

	public VehicleSpeedWidget(MapActivity activity) {
				
		super(activity, 0, activity.getMapLayers().getMapInfoLayer().getPaintText(), activity.getMapLayers().getMapInfoLayer().getPaintSubText());
		setText("-", "km/h");
		///setRecordListener(vehicleSpeedWidget, activity);
		//mapInfoLayer.getMapInfoControls().registerSideWidget(vehicleSpeedWidget,
		//		R.drawable.widget_icon_av_inactive, R.string.map_widget_vehiclediagnostics_speed, "vehiclespeed", false,
		//		EnumSet.allOf(ApplicationMode.class),
		//		EnumSet.noneOf(ApplicationMode.class), 20);
	}
}
