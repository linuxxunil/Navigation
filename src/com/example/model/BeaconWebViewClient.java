package com.example.model;

import java.util.Map;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public abstract class BeaconWebViewClient extends WebViewClient {
	protected WebView view;

	abstract public void doJsFoundBeacon(String json) ;
	abstract public void doJsRemoveBeacon(String uuid, String major, String minor) ;
	
	@Override
	public void onPageFinished(WebView view, String url) {
		this.view = view;
	}
}
