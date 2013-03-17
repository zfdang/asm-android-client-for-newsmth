package com.athena.asm.service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.util.FileUtil;

public class UpdateService extends Service {
	private static final int TIMEOUT = 10 * 1000;// 超时
	private static final String APK_URL = "https://github.com/zfdang/asm-android-client-for-newsmth/raw/master/dist/aSM.apk";

	private static final int DOWN_IN_PROGRESS = 0;
	private static final int DOWN_OK = 1;
	private static final int DOWN_ERROR = 2;

	private String app_name;

	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private Notification.Builder mBuilder;

	private Intent mUpdateIntent;
	private PendingIntent mPendingIntent;

	private static final int NOTIFICATION_ID = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		app_name = intent.getStringExtra("app_name");
		FileUtil.createFile(app_name);
		createNotification();
		createThread();

		return super.onStartCommand(intent, flags, startId);
	}

	// this handler is responsible to update the progress bar
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OK:
				// download finished, click to install
				Uri uri = Uri.fromFile(FileUtil.updateFile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "application/vnd.android.package-archive");
				mPendingIntent = PendingIntent.getActivity(UpdateService.this, 0, intent, 0);

				mBuilder.setContentText("下载成功，点击安装");
				mBuilder.setProgress(100, 100, true);
				mBuilder.setAutoCancel(true);
				mNotification = mBuilder.getNotification();
				mNotification.contentIntent = mPendingIntent;

				mNotificationManager.notify(NOTIFICATION_ID, mNotification);

				stopService(mUpdateIntent);
				break;
			case DOWN_IN_PROGRESS:
				int updateCount = msg.arg1;
				// update progress bar
				mBuilder.setContentText(updateCount + "%");
				mBuilder.setProgress(100, updateCount, false);
				mNotification = mBuilder.getNotification();
				// show notification
				mNotificationManager.notify(NOTIFICATION_ID, mNotification);
				stopService(mUpdateIntent);
				break;
			case DOWN_ERROR:
				mBuilder.setContentText("下载失败");
				mBuilder.setProgress(100, 100, true);
				mBuilder.setAutoCancel(true);
				mNotification = mBuilder.getNotification();
				mNotificationManager.notify(NOTIFICATION_ID, mNotification);
				break;
			default:
				stopService(mUpdateIntent);
				mNotificationManager.cancel(NOTIFICATION_ID);
				break;
			}
		}
	};

	/***
	 * 开线程下载
	 */
	public void createThread() {

		final Message message = new Message();

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					long downloadSize = downloadUpdateFile(APK_URL, FileUtil.updateFile.toString());
					if (downloadSize > 0) {
						// 下载成功
						message.what = DOWN_OK;
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
					message.what = DOWN_ERROR;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	/***
	 * 创建通知栏
	 */

	public void createNotification() {
		mBuilder = new Notification.Builder(this)
		 .setContentTitle("正在下载 aSM.apk")
		 .setContentText("0%")
		 .setProgress(100, 0, false)
		 .setSmallIcon(R.drawable.icon)
		 .setOngoing(true)
		 .setTicker(getString(R.string.update_service_ticker));
		
		mNotification = mBuilder.getNotification();

		mUpdateIntent = new Intent(this, HomeActivity.class);
		mPendingIntent = PendingIntent.getActivity(this, 0, mUpdateIntent, 0);
		mNotification.contentIntent = mPendingIntent;

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}

	/***
	 * 下载文件
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public long downloadUpdateFile(String down_url, String file) throws Exception {
		int down_step = 5;// 提示step
		int totalSize;// 文件总大小
		int downloadCount = 0;// 已经下载好的大小
		int updateCount = 0;// 已经上传的文件大小
		InputStream inputStream;
		OutputStream outputStream;

		URL url = new URL(down_url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// 获取下载文件的size
		totalSize = httpURLConnection.getContentLength();
		if (httpURLConnection.getResponseCode() == 404) {
			throw new Exception("fail!");
		}

		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
		byte buffer[] = new byte[1024];
		int readsize = 0;
		while ((readsize = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, readsize);
			downloadCount += readsize;// 时时获取下载到的大小
			/**
			 * 每次增张5%
			 */
			if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
				updateCount += down_step;

				// call handler to upgrade progree bar
				final Message message = new Message();
				message.what = DOWN_IN_PROGRESS;
				message.arg1 = updateCount;
				handler.sendMessage(message);
			}
		}
		if (httpURLConnection != null) {
			httpURLConnection.disconnect();
		}
		inputStream.close();
		outputStream.close();

		return downloadCount;
	}

}
