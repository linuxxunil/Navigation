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
					i++;
				}
			}
		} catch (FileNotFoundException e) {
			e.getStackTrace();
			cfg = null;
		} catch (IOException e1) {
			e1.getStackTrace();
			cfg = null;
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
		return cfg;
	}
}
