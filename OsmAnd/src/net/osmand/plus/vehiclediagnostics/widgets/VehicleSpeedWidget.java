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
public class VehicleSpeedWidget extends TextInfoWidget {
	
	private VehicleModel vehicleModel;

	public VehicleSpeedWidget(MapActivity activity, VehicleModel vehicleModel) {
				
		super(activity, 0, activity.getMapLayers().getMapInfoLayer().getPaintText(), activity.getMapLayers().getMapInfoLayer().getPaintSubText());
		
		this.vehicleModel = vehicleModel;
		
		setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
				refresh();
			}
		});
		refresh();
	}
	
	public void refresh() {
		
		setText(String.valueOf(vehicleModel.getCurrentVelocity()), "km/h");
	}
}
