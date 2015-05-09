package com.example.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.example.logging.LogString;
import com.example.logging.Log;
import com.example.logging.LogCode;

import android.os.AsyncTask;


public class HttpClient {
	private URL url = null;
	private HttpURLConnection httpConn = null;
	
	public HttpClient() {
	}

	private int initHttp(String host, String method) {
		try {
			url = new URL(host);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod(method);
			httpConn.setRequestProperty("content-type",
					"application/x-www-form-urlencoded; charset=utf-8");
			httpConn.setRequestProperty("host", url.getHost());
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
		} catch (MalformedURLException e1) {
			return Log.e(this, LogCode.ERR_MALFORMED_FAIL);
		} catch (IOException e2) {
			return Log.e(this, LogCode.ERR_HTTP_ATTR_SETTING_FAIL);
		}
		return LogCode.success;
	}

	private int connect() {
		try {
			httpConn.connect();
		} catch (IOException e) {
			return Log.e(this, LogCode.WAR_CONNECT_FAIL);
		}
		return LogCode.success;
	}

	private void disconnect() {
		httpConn.disconnect();
	}
	
	private LogString doPost(final String host, final String content) {
		String cv = "";
		int status = 0;
		OutputStream os;
		
		if ((status = initHttp(host, "POST"))
							!= LogCode.success ) 
			return new LogString(status);
		
		if ((status = connect())
							!= LogCode.success) 
			return new LogString(status);
		
		try {
			os = httpConn.getOutputStream();
			os.write(content.getBytes());
			status = httpConn.getResponseCode();
		
			if (status == HttpURLConnection.HTTP_OK) {
				InputStream is = httpConn.getInputStream();
				byte[] data = new byte[1024];
				int idx = is.read(data);
				cv = new String(data, 0, idx);
					
				is.close();
				os.close();
				disconnect();
			}
		} catch (IOException e) {
			return new LogString(Log.e(this, LogCode.ERR_HTTP_WRITE_CONTENT_FAIL));
		}
		return new LogString(status, cv);
	}

	public LogString post(final String host, final String content)  {
		LogString logStr = null;
		BackGround bg = new BackGround();
		bg.execute(host,content);
		
		try {
			logStr = bg.get();
		} catch (InterruptedException e) {
			return new LogString(Log.e(this, LogCode.ERR_HTTP_INTERRUPT));
		} catch (ExecutionException e) {
			return new LogString(Log.e(this, LogCode.ERR_HTTP_EXECUTE_FAIL));
		}
		return logStr;
	}

	private class BackGround extends AsyncTask <String, String, LogString> {

		@Override
		protected LogString doInBackground(String... params) {
			String host = params[0];
			String content = params[1];
			return doPost(host,content);
		}	 
	}
	
	static public Map<String, String> httpParmToMap(String content) {
		String[] params = content.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}
}	
