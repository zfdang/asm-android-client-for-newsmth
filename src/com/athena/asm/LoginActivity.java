package com.athena.asm;

import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

		String userName = HomeActivity.application.getAutoUserName();
		String password = HomeActivity.application.getAutoPassword();

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
			final String newUserName = userNameEditText.getText().toString();
			final String newPassword = passwordEditText.getText().toString();

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
						HomeActivity.application.updateAutoUserNameAndPassword(newUserName, newPassword);
						
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
			Boolean rememberUser = HomeActivity.application.isRememberUser();
			if (!rememberUser) {
				HomeActivity.application.updateAutoUserNameAndPassword("", "");
			}
			HomeActivity.application.syncPreferences();

			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
