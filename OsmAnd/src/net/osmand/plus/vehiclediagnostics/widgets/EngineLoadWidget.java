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
public class EngineLoadWidget extends TextInfoWidget {
	
	private VehicleModel vehicleModel;

	public EngineLoadWidget(MapActivity activity, VehicleModel vehicleModel) {

		super(activity, 0, activity.getMapLayers().getMapInfoLayer()
				.getPaintText(), activity.getMapLayers().getMapInfoLayer()
				.getPaintSubText());
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
		
		setText(String.format("%.2f%n", ((vehicleModel.getCurrentEngineLoad() * 100.0) / 255.0)), "%");
	}
}
