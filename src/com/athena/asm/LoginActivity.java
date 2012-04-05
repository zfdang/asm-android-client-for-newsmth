package com.athena.asm;

import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
	private EditText m_userNameEditText;
	private EditText m_passwordEditText;

	private SmthSupport m_smthSupport;

	private Handler m_handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);

		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("aSM");

		m_smthSupport = SmthSupport.getInstance();

		String userName = HomeActivity.m_application.getAutoUserName();
		String password = HomeActivity.m_application.getAutoPassword();

		m_userNameEditText = (EditText) findViewById(R.id.username_edit);
		m_userNameEditText.setText(userName);
		m_passwordEditText = (EditText) findViewById(R.id.password_edit);
		m_passwordEditText.setText(password);

		TextView registerLink = (TextView) findViewById(R.id.register_link);
		registerLink.setMovementMethod(LinkMovementMethod.getInstance());

		Button button = (Button) findViewById(R.id.signin_button);
		button.setOnClickListener(this);

		Button gbutton = (Button) findViewById(R.id.guest_button);
		gbutton.setOnClickListener(this);		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	public void showSuccessToast() {
		m_handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), "登录成功.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void showFailedToast() {
		m_handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "用户名或密码错.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View view) {

		m_smthSupport.restore();

		if (view.getId() == R.id.signin_button) {
			final String newUserName = m_userNameEditText.getText().toString();
			final String newPassword = m_passwordEditText.getText().toString();

			// login
			final ProgressDialog pdialog = new ProgressDialog(this);
			pdialog.setMessage("登陆中...");
			// pdialog.setMax(1);
			// pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pdialog.show();

			Thread th = new Thread() {
				@Override
				public void run() {
					m_smthSupport.setUserid(newUserName);
					m_smthSupport.setPasswd(newPassword);
					boolean result = m_smthSupport.login();
					if (!result) {
						showFailedToast();
					} else {
						// showSuccessToast();
						HomeActivity.m_application.updateAutoUserNameAndPassword(newUserName, newPassword);
						
						Intent intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.HomeActivity");
						intent.putExtra(StringUtility.LOGINED, true);
						intent.putExtra(StringUtility.GUEST_LOGINED, false);
						intent.putExtra(StringUtility.LOGINED_ID, newUserName);
						startActivity(intent);
						finish();
					}
					pdialog.cancel();
				}
			};
			th.start();
		} else if (view.getId() == R.id.guest_button) {
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm", "com.athena.asm.HomeActivity");
			intent.putExtra(StringUtility.LOGINED, false);
			intent.putExtra(StringUtility.GUEST_LOGINED, true);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Boolean rememberUser = HomeActivity.m_application.isRememberUser();
			if (!rememberUser) {
				HomeActivity.m_application.updateAutoUserNameAndPassword("", "");
			}
			HomeActivity.m_application.syncPreferences();

			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
