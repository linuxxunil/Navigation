package com.example.model;

import android.util.Log;

public class Beacon extends BeaconBase{
	private int monitor = MonitorMode.LEAVE;
	private int interval = 5; // for default
	private int count = interval;
	private double userDefMeter = 10; // for notify use, default = 10 meter	
	private boolean lowBatteryFlg = false;
	private boolean pressFlg = false;
	private boolean enter1MeterFlg = false;

	
	static public class MonitorMode {
		static final public int LEAVE = 0;
		static final public int ENTER = 1;
		static final public int ENTER_USER_DEF_METER = 2;
		static final public int LEAVE_USER_DEF_METER = 3;
		static final public int ENTER_1_METER = 4;
		static final public int LEAVE_1_METER = 5;
	}
	
	public Beacon (String mac, String uuid, int major, int minor) {
		super(mac, uuid, major, minor);
	}

	public double getUserDefMeter() {
		return userDefMeter;
	}
	
	public void setUserDefMeter(double meter) {
		userDefMeter = meter;
	}
	
	public int getMonitorMode() {
		return monitor;
	}
	
	public boolean getBatteryFlg() {
		return lowBatteryFlg;
	}
	
	public void setBatteryFlg(boolean flg) {
		lowBatteryFlg = flg;
	}
	
	public boolean getPressFlg() {
		return pressFlg;
	}
	
	public void setPressFlg(boolean flg) {
		pressFlg = flg;
	}
		
	/**
	 * 
	 * @param m
	 */
	public boolean setMonitorMode(int m) {
		boolean status = true;
		if ( m == Beacon.MonitorMode.ENTER || 
				m == Beacon.MonitorMode.ENTER_USER_DEF_METER ||
				m == Beacon.MonitorMode.ENTER_1_METER ||
				m == Beacon.MonitorMode.LEAVE ||
				m == Beacon.MonitorMode.LEAVE_USER_DEF_METER ||
				m == Beacon.MonitorMode.LEAVE_1_METER 
				) {
			monitor = m;
		} else status = false ;
		return status;
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
		count = interval;
	}
	
	public int getInterval() {
		return interval;
	}
	
	public void setInterval(int time) {
		interval = time;
	}	
}
