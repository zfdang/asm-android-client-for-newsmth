package com.athena.asm;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.Adapter.AttachListAdapter;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.UploadAttachFilesTask;

public class AttachUploadActivity extends Activity implements OnClickListener {
	
	static final int SELECT_FILE_REQUEST = 0;
	
	public SmthSupport smthSupport;
	
	private LayoutInflater inflater;

	public ArrayList<File> attachArrayList;
	private AttachListAdapter attachListAdapter;
	
	private Button addAttachButton;
	private Button uploadButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.attach_list);

		smthSupport = SmthSupport.getInstance();
		
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		attachArrayList = new ArrayList<File>();		
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("上传附件");
		
		String uploadString = smthSupport.getUrlContent("http://www.newsmth.net/bbsupload.php");
		if (uploadString.contains("选择需要上传的文件后点上传")) {
			addAttachButton = (Button) findViewById(R.id.btn_select_file);
			addAttachButton.setOnClickListener(this);
			
			uploadButton = (Button) findViewById(R.id.btn_start_upload_attach);
			uploadButton.setOnClickListener(this);
			
			attachListAdapter = new AttachListAdapter(this, inflater);
			
			ListView listView = (ListView) findViewById(R.id.attach_list);
			listView.setAdapter(attachListAdapter);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_select_file) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.athena.asm",
                            "com.athena.asm.FileChooserActivity");
            startActivityForResult(intent, SELECT_FILE_REQUEST);
		}
		else if (v.getId() == R.id.btn_start_upload_attach) {
			UploadAttachFilesTask uploadAttachFilesTask = new UploadAttachFilesTask(this);
			uploadAttachFilesTask.execute();
		}
	}
	
	public void uploadFinish() {
//		uploadButton.setText("完成");
//		uploadButton.setEnabled(false);
//		addAttachButton.setEnabled(false);
		
		Toast.makeText(getApplicationContext(), "附件已上传.",
				Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent();
        intent.putExtra(StringUtility.STATUS_OK, "OK");
        setResult(RESULT_OK, intent);
        finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_FILE_REQUEST) {
			if (resultCode == RESULT_OK) {
				attachArrayList.add((File) data
						.getSerializableExtra(StringUtility.SELECTED_FILE));
				attachListAdapter.notifyDataSetChanged();
			}
		}
	}
}
