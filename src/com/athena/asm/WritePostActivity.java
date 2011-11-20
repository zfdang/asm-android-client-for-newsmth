package com.athena.asm;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

public class WritePostActivity extends Activity implements OnClickListener {
	private EditText titleEditText;
	private EditText contentEditText;

	private SmthSupport smthSupport;

	private Handler handler = new Handler();

	private String toHandleUrl;
	private String postUrl;
	private String postTitle;
	private String postContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_reply);

		smthSupport = SmthSupport.getInstance();

		titleEditText = (EditText) findViewById(R.id.post_title);
		contentEditText = (EditText) findViewById(R.id.post_content);

		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("写帖子");

		Button button = (Button) findViewById(R.id.btn_send_post);
		button.setOnClickListener(this);

		toHandleUrl = getIntent().getStringExtra(StringUtility.URL);
		parseToHandleUrl();
	}

	private void parseToHandleUrl() {
		postUrl = "http://www.newsmth.net/bbssnd.php";
		boolean isReply = false;
		Map<String, String> paramsMap = StringUtility.getUrlParams(toHandleUrl);
		if (paramsMap.containsKey("board")) {
			postUrl += "?board=" + paramsMap.get("board");
		}
		if (paramsMap.containsKey("reid")) {
			postUrl += "&reid=" + paramsMap.get("reid");
			isReply = true;
		}

		String contentString = smthSupport.getUrlContent(toHandleUrl);
		Pattern p = Pattern.compile("replyForm\\('[^']+',\\d+,'([^']+)',\\d+");
		Matcher m = p.matcher(contentString);
		if (m.find()) {
			postTitle = m.group(1);
			if (!postTitle.contains("Re:") && isReply) {
				postTitle = "Re:" + postTitle;
			}
			titleEditText.setText(postTitle);
		}

		p = Pattern.compile("-->[\\s\\S]*</script>([\\s\\S]*)</textarea>");
		m = p.matcher(contentString);
		if (m.find()) {
			postContent = m.group(1);
			postContent = postContent.replace("】", "】<br/>");
			contentEditText.setText(Html.fromHtml("<br /><br /><br />"
					+ postContent));
		}

	}

	public void showSuccessToast() {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), "发表成功.",
						Toast.LENGTH_SHORT).show();
			}
		});
		this.finish();
	}

	public void showFailedToast() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "发表失败.",
						Toast.LENGTH_SHORT).show();
			}
		});
		this.finish();
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.btn_send_post) {
			final ProgressDialog pdialog = new ProgressDialog(this);
			pdialog.setMessage("发表中...");
			pdialog.show();

			postTitle = titleEditText.getText().toString();
			postContent = contentEditText.getText().toString();

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(contentEditText.getWindowToken(),
							0);

			Thread th = new Thread() {
				@Override
				public void run() {
					boolean result = smthSupport.sendPost(postUrl, postTitle,
							postContent);
					if (!result) {
						showFailedToast();
					} else {
						showSuccessToast();
					}
					pdialog.cancel();
				}
			};
			th.start();
		}
	}
}
