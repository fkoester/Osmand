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
public class RangeWidget extends TextInfoWidget {

	public RangeWidget(MapActivity activity) {
		
		super(activity, 0, activity.getMapLayers().getMapInfoLayer().getPaintText(), activity.getMapLayers().getMapInfoLayer().getPaintSubText());
	}
}
