package com.example.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class BeaconOpt {
	private Hashtable list = new Hashtable<String,Beacon>();
	public int length = -1;
	static public BeaconOpt create() {
		return new BeaconOpt();
	}
	
	public boolean containsUUID(String uuid) {
		return list.containsKey(uuid);
	}
	
	/**
	 * 
	 * @param i : start is zero
	 * @return beacon,or null if i greater than length
	 */
	public Beacon get(int i) {
		if ( i > length ) return null;
		Iterator it = list.entrySet().iterator();
		int j = 0;
		while(it.hasNext()) {
			if ( i == j++ ) break;	
			it.next();
		}
		return (Beacon) it.next();
	}
	
	public int register(String uuid, Beacon beacon) {
		if ( list.put(uuid, beacon) == null ) {
			length++;
		} else return -1; // already register
		return 0;
	}
	
	public int unregister(String uuid) {
		if (list.remove(uuid) == null ) {
			return -1; // not register
		} else length--;
		return 0;
	}
	
	public void clear() {
		list.clear();
		length = -1;
	}
}
