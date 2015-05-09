package com.example.logging;
import com.example.logging.LogCode;

public class LogInteger extends LogBase{
	
	public int value = 0;
	
	public LogInteger(int status) {
		super(status);
	}
	
	public LogInteger(int status,  int value) {
		super(status);
		this.value = value;
	}
}
