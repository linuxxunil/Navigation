package com.example.logging;
import com.example.logging.LogCode;

public class LogString extends LogBase{
	
	public String value = null;
	
	public LogString(int status) {
		super(status);
	}
	
	public LogString(int status, String str) {
		super(status);
		this.value = str;
	}
	
	public LogString(String str) {
		super(LogCode.success);
		this.value = str;
	}
}
