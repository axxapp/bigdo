package com.bigdo.app;

import java.util.ArrayList;
import java.util.HashMap;

import com.bigdo.controls.IXListViewListener;
import com.bigdo.controls.XListView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class Main_Activity extends Activity implements IXListViewListener {
	private final int waitime = 1000 * 10;
	ImageView splash_log;
	XListView video_list;
	SimpleAdapter ad;
	ArrayList<HashMap<String,Object>> data;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
		splash_log = (ImageView) findViewById(R.id.splash_log_main_activity);
		video_list = (XListView) findViewById(R.id.video_list);
		splash_log.setVisibility(ImageView.VISIBLE);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				splash_log.setVisibility(ImageView.GONE);
			}
		}, waitime);
		
		video_list.setPullLoadEnable(true);
		video_list.setXListViewListener(this);
		data = new ArrayList<HashMap<String,Object>>();
		ad = new SimpleAdapter(this, data, 0, null, null);
		video_list.setAdapter(ad);
		
		 
	}
	@Override
	public void onRefresh(XListView xListView, int tag, int requestCode) {
		// TODO Auto-generated method stub
		xListView.stopRefresh();
		
	}
	@Override
	public void onLoadMore(XListView xListView, int tag, int requestCode) {
		// TODO Auto-generated method stub
		xListView.stopLoadMore();
	}
}
