package com.bigdo.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

public class Main_Activity extends Activity {
	private final int waitime = 1000 * 10;
	ImageView splash_log;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
		splash_log = (ImageView) findViewById(R.id.splash_log_main_activity);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				splash_log.setVisibility(ImageView.GONE);
			}
		}, waitime);
	}
}
