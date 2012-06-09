package com.athena.asm.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.athena.asm.HomeActivity;

public class LogoutService extends IntentService {

	public LogoutService() {
		super("LogoutService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("asm", "start logout");
		if (HomeActivity.m_application != null) {
			if (HomeActivity.m_application.getHomeViewModel() != null) {
				HomeActivity.m_application.getHomeViewModel().logout();
			}
		}
		Log.d("asm", "logout finish");
	}

}
