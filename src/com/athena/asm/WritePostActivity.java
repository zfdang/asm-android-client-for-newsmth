package com.athena.asm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

public class WritePostActivity extends Activity implements OnClickListener,
        OnItemSelectedListener {
	static final int ATTACH_REQUST = 0;
	
    private EditText titleEditText;
    private EditText useridEditText;
    private EditText contentEditText;
    private Spinner sigSpinner;
    private Button attachButton;

    private SmthSupport smthSupport;

    private Handler handler = new Handler();

    private String toHandleUrl;
    private int writeType; // 0 is post, 1 is mail
    private String postUrl = "";
    private String postTitle = "";
    private String postContent = "";
    private String userid = "";
    private String num = "";
    private String dir = "";
    private String file = "";
    private int sigNum = 0;
    private int selectedSigValue = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.post_reply);

        smthSupport = SmthSupport.getInstance();

        titleEditText = (EditText) findViewById(R.id.post_title);
        useridEditText = (EditText) findViewById(R.id.post_userid);
        contentEditText = (EditText) findViewById(R.id.post_content);
        sigSpinner = (Spinner) findViewById(R.id.sig_spinner);

        TextView titleTextView = (TextView) findViewById(R.id.title);

        Button sendButton = (Button) findViewById(R.id.btn_send_post);
        sendButton.setOnClickListener(this);

        attachButton = (Button) findViewById(R.id.btn_attach);
        attachButton.setOnClickListener(this);

        toHandleUrl = getIntent().getStringExtra(StringUtility.URL);
        writeType = getIntent().getIntExtra(StringUtility.WRITE_TYPE, 0);
        if (writeType == 0) {
            ((LinearLayout) useridEditText.getParent())
                    .setVisibility(View.GONE);
            ((LinearLayout) useridEditText.getParent()).removeView(sendButton);
            LinearLayout layout = (LinearLayout) findViewById(R.id.post_second_layout);
            layout.addView(sendButton);
            titleTextView.setText("写帖子");
            parsePostToHandleUrl();
        } else {
            attachButton.setVisibility(View.GONE);
            titleTextView.setText("写  信");
            parseMailToHandleUrl();
        }

        ArrayList<String> list = new ArrayList<String>();
        list.add("不使用签名档");
        for (int i = 1; i <= sigNum; i++) {
            list.add("第" + i + "个");
        }
        if (sigNum > 0) {
            list.add("随机签名档");
        }
        ArrayAdapter<String> sigSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        sigSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sigSpinner.setAdapter(sigSpinnerAdapter);
        sigSpinner.setOnItemSelectedListener(this);
        if (sigNum > 0) {
            if (selectedSigValue != -1) {
                sigSpinner.setSelection(selectedSigValue);
            }
            else {
                sigSpinner.setSelection(sigNum + 1);
            }
        }

        boolean isReply = getIntent().getBooleanExtra(StringUtility.IS_REPLY,
                false);
        if (isReply) {
            contentEditText.requestFocus();
            contentEditText.setSelection(0);
        }
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

    private void parsePostToHandleUrl() {
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
        // function replyForm(board,reid,title,att,signum,sig,ano,outgo,lsave)
        Pattern p = Pattern
                .compile("replyForm\\('[^']+',\\d+,'([^']+)',\\d+,(\\d+),([+-]?\\d+)");
        Matcher m = p.matcher(contentString);
        if (m.find()) {
            postTitle = m.group(1);
            sigNum = Integer.parseInt(m.group(2));
            selectedSigValue = Integer.parseInt(m.group(3));
            if (!postTitle.contains("Re:") && isReply) {
                postTitle = "Re: " + postTitle;
            }
            titleEditText.setText(postTitle);
        }

        p = Pattern.compile("-->[\\s\\S]*</script>([\\s\\S]*)</textarea>");
        m = p.matcher(contentString);
        if (m.find()) {
            postContent = m.group(1);
            postContent = postContent.replace("\n", "\n<br/>");
            if (HomeActivity.application.isPromotionShow()) {
            	postContent += "--<br/>发送自aSM水木客户端\n<br/>";
			}
            contentEditText.setText(Html.fromHtml("<br/>" + postContent));
        }

    }

    private void parseMailToHandleUrl() {
        postUrl = "http://www.newsmth.net/bbssendmail.php";
        Map<String, String> paramsMap = StringUtility.getUrlParams(toHandleUrl);
        if (paramsMap.containsKey("dir")) {
            dir = paramsMap.get("dir");
            postUrl += "?dir=" + dir;
        }
        if (paramsMap.containsKey("userid")) {
            userid = paramsMap.get("userid");
            postUrl += "?userid=" + userid;
        }
        if (paramsMap.containsKey("num")) {
            num = paramsMap.get("num");
            postUrl += "?num=" + num;
        }
        if (paramsMap.containsKey("file")) {
            file = paramsMap.get("file");
            postUrl += "?file=" + file;
        }
        if (paramsMap.containsKey("title")) {
            try {
                postTitle = URLDecoder.decode(paramsMap.get("title"), "GBK");
                if (!postTitle.contains("Re:")) {
                    postTitle = "Re: " + postTitle;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            postUrl += "?title=" + paramsMap.get("title");
            titleEditText.setText(postTitle);
            Button button = (Button) findViewById(R.id.btn_send_post);
            ((LinearLayout) useridEditText.getParent())
                    .setVisibility(View.GONE);
            ((LinearLayout) useridEditText.getParent()).removeView(button);
            LinearLayout layout = (LinearLayout) findViewById(R.id.post_first_layout);
            layout.addView(button);
        }

        String contentString = smthSupport.getUrlContent(toHandleUrl);

        sigNum = StringUtility.getOccur(contentString, "<option") - 2;
        Pattern p = Pattern
                .compile("option value=\"([+-]?\\d+)\" selected");
        Matcher m = p.matcher(contentString);
        if (m.find()) {
            selectedSigValue = Integer.parseInt(m.group(1));
        }

        Pattern pattern = Pattern
                .compile("<textarea[^<>]+>([^<>]+)</textarea>");
        Matcher matcher = pattern.matcher(contentString);
        if (matcher.find()) {
            postContent = matcher.group(1);
            postContent = postContent.replace("\n", "\n<br/>");
            contentEditText.setText(Html.fromHtml("<br />" + postContent));
        }

        if (paramsMap.containsKey("board")) {
            pattern = Pattern
                    .compile("<input class=\"sb1\" type=\"text\" name=\"title\"[^<>]+value=\"([^<>]+)\">");
            matcher = pattern.matcher(contentString);
            if (matcher.find()) {
                postTitle = matcher.group(1);
                if (!postTitle.contains("Re:")) {
                    postTitle = "Re: " + postTitle;
                }
                titleEditText.setText(postTitle);
            }
            pattern = Pattern
                    .compile("<input class=\"sb1\" type=\"text\" name=\"userid\" value=\"([^<>]+)\">");
            matcher = pattern.matcher(contentString);
            if (matcher.find()) {
                useridEditText.setText(matcher.group(1));
            }
        }
    }

    public void showSuccessToast() {
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "发表成功.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        if (writeType == 0) {
            Intent i = new Intent();

            Bundle b = new Bundle();
            b.putBoolean(StringUtility.REFRESH_BOARD, true);
            i.putExtras(b);

            this.setResult(RESULT_OK, i);
        }
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
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ATTACH_REQUST) {
			if (resultCode == RESULT_OK) {
				attachButton.setEnabled(false);
			}
		}
	}

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_attach) {
            Intent intent = new Intent();
            intent.setClassName("com.athena.asm",
                            "com.athena.asm.AttachUploadActivity");
            startActivityForResult(intent, ATTACH_REQUST);
        }
        else if (view.getId() == R.id.btn_send_post) {
            final ProgressDialog pdialog = new ProgressDialog(this);
            pdialog.setMessage("发表中...");
            pdialog.show();

            postTitle = titleEditText.getText().toString();
            if (userid.length() < 1) {
                userid = useridEditText.getText().toString().trim();
            }
            postContent = contentEditText.getText().toString();

            int selectedSig = sigSpinner.getSelectedItemPosition();
            if (selectedSig == sigNum + 1) {
                selectedSig = -1;
            }
            final String sigParams = String.valueOf(selectedSig);

            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(contentEditText.getWindowToken(),
                            0);

            Thread th = new Thread() {
                @Override
                public void run() {
                    boolean result = false;
                    if (writeType == 0) {
                        result = smthSupport.sendPost(postUrl, postTitle,
                                postContent, sigParams);
                    } else {
                        result = smthSupport.sendMail(postUrl, postTitle,
                                userid, num, dir, file, sigParams, postContent);
                    }
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

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
