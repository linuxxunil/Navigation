package com.example.logging;

import com.example.model.Beacon;

public class LogBeacon extends LogBase{
	
	public Beacon value = null;
	
	public LogBeacon(int status) {
		super(status);
	}
	
	public LogBeacon(int status, Beacon value) {
		super(status);
		this.value = value;
	}
	
	public LogBeacon(Beacon value) {
		super(LogCode.success);
		this.value = value;
	}
}
