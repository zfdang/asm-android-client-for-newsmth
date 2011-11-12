package com.athena.asm;

import com.athena.asm.data.Preferences;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	private EditText userNameEditText;
	private EditText passwordEditText;

	private SmthSupport smthSupport;

	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("aSM");

		smthSupport = SmthSupport.getInstance();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		if (!settings.contains(Preferences.REMEMBER_USER)) {
			editor.putBoolean(Preferences.REMEMBER_USER, true);
		}
		if (!settings.contains(Preferences.DEFAULT_TAB)) {
			editor.putString(Preferences.DEFAULT_TAB, "001");
		}
		if (!settings.contains(Preferences.DEFAULT_BOARD_TYPE)) {
			editor.putString(Preferences.DEFAULT_BOARD_TYPE, "001");
		}
		editor.commit();
		String userName = settings.getString(Preferences.USERNAME_KEY, "");
		String password = settings.getString(Preferences.PASSWORD_KEY, "");

		userNameEditText = (EditText) findViewById(R.id.username_edit);
		userNameEditText.setText(userName);
		passwordEditText = (EditText) findViewById(R.id.password_edit);
		passwordEditText.setText(password);

		TextView registerLink = (TextView) findViewById(R.id.register_link);
		registerLink.setMovementMethod(LinkMovementMethod.getInstance());

		Button button = (Button) findViewById(R.id.signin_button);
		button.setOnClickListener(this);

		Button gbutton = (Button) findViewById(R.id.guest_button);
		gbutton.setOnClickListener(this);
	}

	public void showSuccessToast() {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), "登录成功.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void showFailedToast() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "用户名或密码错.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View view) {

		smthSupport.restore();

		if (view.getId() == R.id.signin_button) {

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			String userid = settings.getString(Preferences.USERNAME_KEY, "");
			String passwd = settings.getString(Preferences.PASSWORD_KEY, "");

			final String newUserName = userNameEditText.getText().toString();
			final String newPassword = passwordEditText.getText().toString();

			SharedPreferences.Editor editor = settings.edit();
			boolean flag = false;
			if (!newUserName.equals(userid)) {
				editor.putString(Preferences.USERNAME_KEY, newUserName);
				flag = true;
			}
			if (!newPassword.equals(passwd)) {
				editor.putString(Preferences.PASSWORD_KEY, newPassword);
				flag = true;
			}
			if (flag) {
				editor.commit();
			}

			// login
			final ProgressDialog pdialog = new ProgressDialog(this);
			pdialog.setMessage("登陆中...");
			// pdialog.setMax(1);
			// pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pdialog.show();

			Thread th = new Thread() {
				@Override
				public void run() {
					smthSupport.setUserid(newUserName);
					smthSupport.setPasswd(newPassword);
					boolean result = smthSupport.login();
					if (!result) {
						showFailedToast();
					} else {
						// showSuccessToast();
						Intent intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.HomeActivity");
						intent.putExtra(StringUtility.LOGINED, true);
						intent.putExtra(StringUtility.LOGINED_ID, newUserName);
						startActivity(intent);
					}
					pdialog.cancel();
				}
			};
			th.start();
		} else if (view.getId() == R.id.guest_button) {
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm", "com.athena.asm.HomeActivity");
			intent.putExtra(StringUtility.LOGINED, false);
			startActivity(intent);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			Boolean rememberUser = settings.getBoolean(
					Preferences.REMEMBER_USER, true);
			if (!rememberUser) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(Preferences.USERNAME_KEY, "");
				editor.putString(Preferences.PASSWORD_KEY, "");
				editor.commit();
			}

			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
