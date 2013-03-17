package com.athena.asm.util.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.athena.asm.R;
import com.athena.asm.service.UpdateService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CheckUpdateAsyncTask extends AsyncTask<Integer, Integer, String> {

	private final static String NO_REMOTE_VERSION = "无法获取最新版本，请稍后重试!";
	private final static String NO_NEW_UPDATE = "您的软件已是最新版本，无需更新!";
	private final static String NEW_UPDATE_AVAILABLE = "亲，有最新的软件包，赶紧下载吧~\n\n本地版本: %s\n最新版本: %s";

	public CheckUpdateAsyncTask(Context context) {
		super();
		this.mContext = context;
	}

	private Context mContext;

	private AlertDialog noticeDialog; // 提示弹出框

	private String localVersionName = "Unknown";
	private String remoteVersionName;

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(mContext);
		pdialog.setMessage("检查新版本中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(Integer... params) {
		// get local version name first
		PackageManager pm = mContext.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			localVersionName = pi.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d("CheckUpdateAsyncTask: can't get local version name", e.toString());
		}

		// get remote version name
		try {
			getUpateApkInfo();
		} catch (Exception e) {
			Log.d("CheckUpdateAsyncTask: can't get remote version name", e.toString());
		}
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		if (remoteVersionName == null) {
			Toast.makeText(mContext, NO_REMOTE_VERSION, Toast.LENGTH_SHORT).show();
		} else if (remoteVersionName.equals(localVersionName)) {
			Toast.makeText(mContext, NO_NEW_UPDATE, Toast.LENGTH_SHORT).show();
		} else {
			showNoticeDialog();
		}
	}

	/**
	 * 弹出软件更新提示对话框
	 */
	private void showNoticeDialog() {
		Builder builder = new AlertDialog.Builder(mContext);
		String title = String.format(NEW_UPDATE_AVAILABLE, localVersionName, remoteVersionName);
		builder.setTitle("软件版本更新").setMessage(title);

		builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 开启更新服务UpdateService
				// 这里为了把update更好模块化，可以传一些updateService依赖的值
				// 如布局ID，资源ID，动态获取的标题,这里以app_name为例
				Intent updateIntent = new Intent(mContext, UpdateService.class);
				updateIntent.putExtra("app_name", mContext.getResources().getString(R.string.app_name));
				mContext.startService(updateIntent);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 获取升级APK详细信息 {apkVersion:
	 * '1.10',apkVerCode:2,apkName:'1.1.apk',apkDownloadUrl:'http://localhost:8080/myapp/1.1.apk
	 * ' }
	 * 
	 * @return
	 */
	private void getUpateApkInfo() {
		// read lated version from this URL:
		// https://raw.github.com/zfdang/asm-android-client-for-newsmth/master/AndroidManifest.xml
		final String manifestURL = "https://raw.github.com/zfdang/asm-android-client-for-newsmth/master/AndroidManifest.xml";
		HttpGet httpGet = new HttpGet(manifestURL);
		String content = null;

		// create https connection
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		HttpParams params = new BasicHttpParams();
		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
		HttpClient client = new DefaultHttpClient(mgr, params);

		try {
			HttpResponse response = client.execute(httpGet);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				InputStream is = entity.getContent();
				BufferedReader br = new java.io.BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				br.close();
				is.close();
				content = sb.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (content != null) {
			// android:versionName="2013.03.17"
			Pattern hp = Pattern.compile("android:versionName=\"(\\S+)\"", Pattern.CASE_INSENSITIVE);
			Matcher hm = hp.matcher(content);
			if (hm.find()) {
				remoteVersionName = hm.group(1);
			}
		}
		// for testing purpose
		// remoteVersionName = "2013.03.18";
	}

}