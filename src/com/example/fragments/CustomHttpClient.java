package com.example.fragments;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class CustomHttpClient {
private static HttpClient customHttpClient;
	private CustomHttpClient(){
	}
	
	public static synchronized HttpClient getHttpClient(){
		if(customHttpClient == null) {
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 3500);
			SchemeRegistry schReg = new SchemeRegistry();
	        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			//ClientConnectionManager
			ThreadSafeClientConnManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customHttpClient = new DefaultHttpClient(conMgr,params);
		}
	 return customHttpClient;	
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
