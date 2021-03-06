package com.example.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;



public class HttpClient {
	private URL url = null;
	private HttpURLConnection httpConn = null;
	private static int i = 0;
	
	public HttpClient() {
	}

	private void initHttp(String host, String method)
			throws MalformedURLException, IOException {
		url = new URL(host);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestMethod(method);
		httpConn.setRequestProperty("content-type",
				"application/x-www-form-urlencoded; charset=utf-8");
		httpConn.setRequestProperty("host", url.getHost());
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		httpConn.setReadTimeout(10000);
	}

	private void connect() throws IOException {
		httpConn.connect();
	}

	private void disconnect() {
		httpConn.disconnect();
	}
	
	private String doPost(final String host, final String content) {
		String cv = "";
		int status = 0;
		OutputStream os;
		try {
			initHttp(host, "POST");
			connect();
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
		} catch (MalformedURLException e) {
			e.getStackTrace();
		} catch (IOException e) {
			e.getStackTrace();
		} 
		return cv;
	}

	public String post(final String host, final String content)  {
		System.out.println(host);
		System.out.println(content);
		String result = doPost(host,content);
		System.out.println(result);
		return result;
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