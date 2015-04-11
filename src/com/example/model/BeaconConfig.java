package com.example.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public class BeaconConfig {
	
	static public Map<String, String> getBeaconConfig(String path) {
		FileReader fr = null;
		BufferedReader br = null;
		
		Map<String, String> cfg = new Hashtable<String, String>();
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			String line = "";
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				String[] str = line.split(",");
				if ( str != null) {
					cfg.put("size", String.valueOf(i+1));
					cfg.put("uuid["+i+"]", str[0]);
					cfg.put("major["+i+"]", str[1]);
					cfg.put("minor["+i+"]", str[2]);
					cfg.put("interval["+i+"]", str[3]);
					cfg.put("distance["+i+"]", str[4]);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Config Not Found :"+e.getMessage());
			cfg = null;
		} catch (IOException e1) {
			System.out.println("IO Error");
			cfg = null;
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e) {
				// nothing
			}
		}
		return cfg;
	}
}
