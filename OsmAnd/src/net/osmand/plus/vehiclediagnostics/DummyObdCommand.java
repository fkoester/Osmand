/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * @author fabian
 *
 */
public class DummyObdCommand extends ObdCommand {
	
	String name;
	
	public DummyObdCommand(String name) {
		super("01 04");
		
		this.name = name;
		
		int value = (int)(Math.random() * 255);
		
		rawData = "41 04 " + Integer.toHexString(value);
		
		buffer.add(Integer.decode("0x41"));
		buffer.add(Integer.decode("0x04"));
		buffer.add(value);
	}

	/**
	 * @see eu.lighthouselabs.obd.commands.ObdCommand#getFormattedResult()
	 */
	@Override
	public String getFormattedResult() {
		float tempValue = (buffer.get(2) * 100.0f) / 255.0f;
		return String.format("%.1f%s", tempValue, "%");
	}

	/**
	 * @see eu.lighthouselabs.obd.commands.ObdCommand#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
}
