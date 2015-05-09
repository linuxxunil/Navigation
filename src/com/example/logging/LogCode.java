package com.example.logging;

/**
 * StatusCode Definition Status Code : 1. status code = 0 => Success 2. status
 * code > 0 => Information 3. status code < 0 => Error 4. consist of F E DD CC B
 * AA (-0 AA : Error Code B : Type * 0: User define Error * 1: Parameter Error *
 * 2: Database common Error * 3: Network Error DD : Class Index DD : Class
 * number E : Reserved F : +/-
 * 
 * @author jesse
 *
 */
public class LogCode {
	final static public int success = 0;
	final static private String projectName = "com.example";
	final static public String[] ClassInfo = {
			projectName + ".logger.Log", 				"00110000",
			projectName + ".activity.MainActivity", 	"00210000",
			projectName + ".model.Beacon",  			"00310000",
			projectName + ".model.BeaconList",  		"00320000",
			projectName + ".model.BeaconScanner",  		"00330000",
			projectName + ".model.service",  			"00410000",
			 };

	// com.example.logger.Log
	final static public String WAR_LOGER_NUMBER_NOT_FOUND = "101,Loger number not define";
	
	// com.example.service.BeaconService
	final static public String WAR_ADD_LEAVE_NLIST = "201,add Leave NList";
	final static public String ERR_SEND_MSG_TO_ACTIVITY_FAIL = "-201,Send message to activity to fail";
	
	// com.example.service.BeaconServiceServiceConnection
	
	// com.example.model.Beacon
	final static public String INF_NOT_FOUND_MONITOR_TYPE = "301,Not found monitor type";
	final static public String ERR_ = "302,Not found monitor type";
	
	// com.example.model.BeaconList
	final static public String INF_REGISTERED = "401,Aready register";
	final static public String INF_NOT_REGISTERED = "401,Not register";
	// com.example.model.BeaconConfig
	
	// com.example.model.BeaconNotification
	
	// com.example.model.BeaconScanner
	
	// com.example.model.BeaconWebVeiwClient
	
	// com.example.model.HttpClient
	final static public String WAR_CONNECT_FAIL = "301,Connect to server is fail";
	final static public String ERR_MALFORMED_FAIL = "-301,Malformed url";
	final static public String ERR_HTTP_ATTR_SETTING_FAIL = "-302, Set Http header fail";
	final static public String ERR_HTTP_WRITE_CONTENT_FAIL = "-303, Send http content fail";
	final static public String ERR_HTTP_INTERRUPT = "-304, Send http content fail";
	final static public String ERR_HTTP_EXECUTE_FAIL = "-305, Send http content fail";
	
	
	
	
	
}
