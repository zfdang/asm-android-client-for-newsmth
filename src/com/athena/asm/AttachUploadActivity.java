package com.athena.asm;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.athena.asm.Adapter.AttachListAdapter;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.UploadAttachFilesTask;

public class AttachUploadActivity extends SherlockActivity implements
		OnClickListener {

	static final int SELECT_FILE_REQUEST = 0;

	public SmthSupport m_smthSupport;

	private LayoutInflater m_inflater;

	public ArrayList<File> m_attachArrayList;
	private AttachListAdapter m_attachListAdapter;

	private Button m_addAttachButton;
	private Button m_uploadButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(HomeActivity.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attach_list);

		m_smthSupport = SmthSupport.getInstance();

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_attachArrayList = new ArrayList<File>();

		setTitle("上传附件");

		m_addAttachButton = (Button) findViewById(R.id.btn_select_file);
		m_addAttachButton.setOnClickListener(this);
		m_addAttachButton.setEnabled(false);

		m_uploadButton = (Button) findViewById(R.id.btn_start_upload_attach);
		m_uploadButton.setOnClickListener(this);
		m_uploadButton.setEnabled(false);

		m_attachListAdapter = new AttachListAdapter(this, m_inflater);

		ListView listView = (ListView) findViewById(R.id.attach_list);
		listView.setAdapter(m_attachListAdapter);

		new LoadUploadInfoTask().execute();

	}

	class LoadUploadInfoTask extends AsyncTask<String, Integer, String> {
		
		private String m_content;

		@Override
		protected String doInBackground(String... arg0) {
			m_content = m_smthSupport
					.getUrlContent("http://www.newsmth.net/bbsupload.php");
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			parseUploadInfo(m_content);
		}

	}

	public void parseUploadInfo(String content) {
		if (content.contains("选择需要上传的文件后点上传")) {
			m_addAttachButton.setEnabled(true);
			m_uploadButton.setEnabled(true);
		} else {
			Toast.makeText(this, "无法上传，请退出重试", Toast.LENGTH_SHORT);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_select_file) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName("com.athena.asm",
					"com.athena.asm.FileChooserActivity");
			startActivityForResult(intent, SELECT_FILE_REQUEST);
		} else if (v.getId() == R.id.btn_start_upload_attach) {
			UploadAttachFilesTask uploadAttachFilesTask = new UploadAttachFilesTask(
					this);
			uploadAttachFilesTask.execute();
		}
	}

	public void uploadFinish() {
		// uploadButton.setText("完成");
		// uploadButton.setEnabled(false);
		// addAttachButton.setEnabled(false);

		Toast.makeText(getApplicationContext(), "附件已上传.", Toast.LENGTH_SHORT)
				.show();

		Intent intent = new Intent();
		intent.putExtra(StringUtility.STATUS_OK, "OK");
		setResult(RESULT_OK, intent);
		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_FILE_REQUEST) {
			if (resultCode == RESULT_OK) {
				m_attachArrayList.add((File) data
						.getSerializableExtra(StringUtility.SELECTED_FILE));
				m_attachListAdapter.notifyDataSetChanged();
			}
		}
	}
}
