package com.zfdang.asm.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.zfdang.asm.aSMApplication;

public class LogoutService extends IntentService {

	public LogoutService() {
		super("LogoutService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("asm", "start logout");
		if (aSMApplication.getCurrentApplication() != null) {
			if (aSMApplication.getCurrentApplication().getHomeViewModel() != null) {
				aSMApplication.getCurrentApplication().getHomeViewModel().logout();
			}
		}
		Log.d("asm", "logout finish");
	}

}
