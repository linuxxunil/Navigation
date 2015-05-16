package com.example.model;


public class BeaconBase {
	private String mac;
	private String uuid;
	private int major;
	private int minor;
	private int rssi;
	static private double n = 2.92;
	static private int benchmark = -70; 
	
	protected BeaconBase(String mac, String uuid, int major, int minor) {
		this.mac = mac;
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
	}

	static public double toMeter(double rssi) {
		double tmp = -(rssi - benchmark) / (10 * n);
		return Math.pow(10, tmp);
	}
	
	public void setBenchmark(int dBmAtOneMeter) {
		benchmark = dBmAtOneMeter;
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
	
	public void setRSSI(int rssi) {
		this.rssi = rssi;
	}
	
	public int getRSSI() {
		return rssi;
	}
}
