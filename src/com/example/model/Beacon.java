package com.example.model;

import android.util.Log;

public class Beacon {
	private final String CLASSNAME = this.getClass().getName();
	private String mac;
	private String uuid;
	private int major;
	private int minor;
	private int monitor = Monitor.LEAVE;
	private int rssi;
	private int interval = 5; // for default
	private int count = interval;
	private double notifyDistance = 10; // for notify use, default = 10 meter
	static private int benchmark = -64; 
	static private double n = 2.92;

	
	static public class Monitor {
		static public int LEAVE = 0;
		static public int FOUND = 1;
		static public int ENTERSCOPE = 2;
	}
	
	private void initParameter(String mac, String uuid, int major, int minor) {
		this.mac = mac;
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
	}
	
	private void initParameter(String mac, String uuid, int major, int minor, int interval, int distance) {
		initParameter(mac, uuid, major, minor);
		this.interval = interval;
		this.count = interval;
		this.notifyDistance = distance;
	}
	
	public Beacon (String mac, String uuid, int major, int minor) {
		initParameter(mac, uuid, major, minor);
	}
	
	public Beacon (String mac, String uuid, int major, int minor, int interval, int distance) {
		initParameter(mac, uuid, major, minor, interval, distance);
	}

	public void setBenchmark(int dBmAtOneMeter) {
		benchmark = dBmAtOneMeter;
	}

	public double toDistance(double rssi) {
		double tmp = -(rssi - benchmark) / (10 * n);
		return Math.pow(10, tmp);
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public String getMAC() {
		return mac;
	}
	
	public double getNotifyDistance() {
		return notifyDistance;
	}
	
	public void setNotifyDistance(double dist) {
		notifyDistance = dist;
	}
	
	public int getMonitorStatus() {
		return monitor;
	}
	
	
	/**
	 * 
	 * @param m
	 */
	public void setMonitorStatus(int m) {
		
		if ( m == Beacon.Monitor.FOUND || 
				m == Beacon.Monitor.ENTERSCOPE || 
				m == Beacon.Monitor.LEAVE) {
			monitor = m;
			return ;
		}
		Log.e(CLASSNAME, "Not found monitor type");
	}
	
	/**
	 * 
	 * @return
	 */
	
	public int doCount() {
		if ( --count < 0 ) {
			count = 0;
		}
		return count;
	}
	
	public void resetCount() {
		Log.i(CLASSNAME, "resetCount");
		count = interval;
	}
	
	public int getInterval() {
		return interval;
	}
	
	public void setInterval(int time) {
		interval = time;
	}	
}