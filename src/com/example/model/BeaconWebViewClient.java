package com.example.model;

import java.util.Map;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public abstract class BeaconWebViewClient extends WebViewClient {
	protected WebView view;

	abstract public void doJavaScript(String uuid, String major,
			String minor, String funcName, Map<String, String> parm) ;
	
	@Override
	public void onPageFinished(WebView view, String url) {
		this.view = view;
	}
}
