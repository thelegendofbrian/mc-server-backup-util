package io.github.thelegendofbrian.utility;

public class Wrapper {
	
	private long value;
	
	public Wrapper(long value) {
		this.value = value;
	}
	
	public Wrapper() {
		this.value = 0;
	}
	
	public long getValue() {
		return value;
	}
	
	public void setValue(long value) {
		this.value = value;
	}
}
