/**
 * 
 */
package net.osmand.plus.vehiclediagnostics;

/**
 * @author fabian
 *
 */
public class Sample<T> {

	private long timestamp;
	private T value;
	
	public Sample(T value) {
		
		this(System.currentTimeMillis(), value);
	}
	
	public Sample(long timestamp, T value) {
		super();
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
}
