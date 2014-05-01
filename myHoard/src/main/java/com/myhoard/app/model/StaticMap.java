package com.myhoard.app.model;

import android.widget.ImageView;

/**
 * Created by Sebastian Peryt on 1.05.14.
 */
public class StaticMap {
	
	private String url;
	private ImageView view;
	
	public StaticMap() {
		
	}
	
	public void put(ImageView view, String url) {
		this.view = view;
		this.url = url;
	}
	
	public String get(ImageView view) {
		return url;
	}
	
	public ImageView get(String url) {
		return view;
	}
}
