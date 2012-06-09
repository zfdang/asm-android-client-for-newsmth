package com.athena.asm.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.viewmodel.HomeViewModel;

public class CheckMessageService extends Service {

	private CheckMessageTask m_checkMessageTask;
	private HomeViewModel m_homeViewModel;
	private NotificationManager m_NotificationManager;
	private Context m_context;
	private static boolean m_isFirstCheck = true;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (HomeActivity.m_application != null) {
			m_homeViewModel = HomeActivity.m_application.getHomeViewModel();

			if (!m_homeViewModel.isLogined() || !HomeActivity.m_application.isShowCheck()) {
				stopSelf();
				return;
			}
			
			m_context = this;

			schedule(CheckMessageService.this);

			m_NotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			if (m_checkMessageTask != null
					&& m_checkMessageTask.getStatus() == AsyncTask.Status.RUNNING) {
				return;
			} else {
				m_checkMessageTask = new CheckMessageTask();
				m_checkMessageTask.execute();
			}
		} else {
			stopSelf();
		}
	}
	
	@Override
	public void onDestroy() {
		if (m_checkMessageTask != null
				&& m_checkMessageTask.getStatus() == CheckMessageTask.Status.RUNNING) {
			m_checkMessageTask.cancel(true);
		}

		super.onDestroy();
	}
	
	public static void schedule(Context context) {
		int interval = Integer.parseInt(HomeActivity.m_application.getCheckInterval());
		Intent intent = new Intent(context, CheckMessageService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar c = new GregorianCalendar();
		if (m_isFirstCheck) {
			m_isFirstCheck = false;
			c.add(Calendar.SECOND, 5);
		} else {
			c.add(Calendar.MINUTE, interval);
		}

		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
	}
	
	public static void unschedule(Context context) {
		Intent intent = new Intent(context, CheckMessageService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
	}
	
	public static int MESSAGE_NOTIFICATION_ID = 0;
	
	private void showNotification(PendingIntent intent, int notificationId,
			int notifyIconId, String tickerText, String title, String text) {
		Notification notification = new Notification(notifyIconId, tickerText,
				System.currentTimeMillis());
		
		notification.setLatestEventInfo(this, title, text, intent);

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONLY_ALERT_ONCE;

		if (HomeActivity.m_application.isUseVibrate()) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		m_NotificationManager.notify(notificationId, notification);
	}
	
	class CheckMessageTask extends AsyncTask<String, Integer, String> {
		
		String m_result;
		
		@Override
		protected String doInBackground(String... arg0) {
			m_result = HomeActivity.m_application.getHomeViewModel().checkNewMessage();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (m_result != null) {
				PendingIntent pendingIntent = PendingIntent.getActivity(m_context, 0,
						new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
				
				String title = "";
				String text = "";
				if (m_result.contains("信件")) {
					title = "新来信";
					text = "您有新信件，收查收";
				} else if (m_result.contains("@")) {
					title = "新@";
					text = "您有新@，快去看看吧";
				} else if (m_result.contains("回复")) {
					title = "新回复";
					text = "您有新回复，快去看看吧";
				}

				showNotification(pendingIntent, MESSAGE_NOTIFICATION_ID, R.drawable.icon, 
						title, title, text);
			}
			
			stopSelf();
		}
	}

}
