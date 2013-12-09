package com.zfdang.asm;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.zfdang.asm.Adapter.AttachListAdapter;
import com.zfdang.asm.util.SmthSupport;
import com.zfdang.asm.util.StringUtility;
import com.zfdang.asm.util.task.UploadAttachFilesTask;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.zfdang.asm.R;

public class AttachUploadActivity extends SherlockActivity implements OnClickListener {

    static final int SELECT_FILE_REQUEST = 0;

    public SmthSupport m_smthSupport;

    private LayoutInflater m_inflater;

    public ArrayList<File> m_attachArrayList;
    private AttachListAdapter m_attachListAdapter;

    private Button m_addAttachButton;
    private Button m_uploadButton;
    private Button m_doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(aSMApplication.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attach_list);

        setTitle("上传附件");

        m_addAttachButton = (Button) findViewById(R.id.btn_select_file);
        m_addAttachButton.setOnClickListener(this);
        m_addAttachButton.setEnabled(false);

        m_uploadButton = (Button) findViewById(R.id.btn_start_upload_attach);
        m_uploadButton.setOnClickListener(this);
        m_uploadButton.setEnabled(false);

        m_doneButton = (Button) findViewById(R.id.btn_upload_done);
        m_doneButton.setOnClickListener(this);

        m_smthSupport = SmthSupport.getInstance();
        m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        m_attachArrayList = new ArrayList<File>();
        m_attachListAdapter = new AttachListAdapter(this, m_inflater);
        ListView listView = (ListView) findViewById(R.id.attach_list);
        listView.setAdapter(m_attachListAdapter);

        // this task will decide whether we can upload attachments now
        new CheckUploadCapabilityTask(this).execute();
    }

    /*
     * this task load the upload page, and check whether we can upload
     * attachments at the moment this task is not responsible for uploading
     * attachments UploadAttachFilesTask.java is for uploading attachments
     */
    class CheckUploadCapabilityTask extends AsyncTask<String, Integer, String> {
        private Context m_context;
        private String m_content;

        public CheckUploadCapabilityTask(Context context) {
            super();
            m_context = context;
        }

        @Override
        protected String doInBackground(String... arg0) {
            m_content = m_smthSupport.getUrlContent("http://www.newsmth.net/bbsupload.php");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // 选择需要上传的文件后点上传：(<a id="idAllAtt" style="display:none;"
            // href="javascript:...
            if (m_content.contains("选择需要上传的文件后点上传")) {
                m_addAttachButton.setEnabled(true);
                m_uploadButton.setEnabled(true);
            } else {
                Toast.makeText(m_context, "无法上传，请重新登录后再试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (m_attachArrayList.size() > 0) {
            // there are files not uploaded, alert users
            Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("放弃附件").setMessage("选择的附件尚未上载，放弃附件么？");
            builder.setPositiveButton("放弃附件", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton("继续操作", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    return;
                }
            });
            AlertDialog noticeDialog = builder.create();
            noticeDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_select_file) {
            showChooser();
        } else if (v.getId() == R.id.btn_start_upload_attach) {
            UploadAttachFilesTask uploadAttachFilesTask = new UploadAttachFilesTask(this);
            uploadAttachFilesTask.execute();
        } else if (v.getId() == R.id.btn_upload_done) {
            onBackPressed();
        }
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, getString(R.string.upload_file_title));
        try {
            startActivityForResult(intent, SELECT_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_FILE_REQUEST) {
            // If the file selection was successful
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();

                    try {
                        // Create a file instance from the URI
                        String filename = FileUtils.getFilenameFromUri(this, uri);
                        final File myFile = new File(filename);
                        m_attachArrayList.add(myFile);
                        m_attachListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("FileSelectorTestActivity", "File select error", e);
                    }
                }
            }
        }
    }

    public void uploadFinish(boolean result) {
        if (result) {
            Toast.makeText(getApplicationContext(), "附件已上传.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(StringUtility.STATUS_OK, "OK");
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "附件上传失败...", Toast.LENGTH_SHORT).show();
        }
    }
}
