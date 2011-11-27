package com.athena.asm;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.athena.asm.Adapter.ReadMailAdapter;
import com.athena.asm.data.Mail;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadMailContentTask;

public class ReadMailActivity extends Activity {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	public Mail currentMail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();

		currentMail = (Mail) getIntent().getSerializableExtra(
				StringUtility.MAIL);

		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(currentMail.getTitle());

		EditText pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setVisibility(View.GONE);
		TextView totalPageNoTextView = (TextView) findViewById(R.id.textview_page_total_no);
		totalPageNoTextView.setVisibility(View.GONE);

		Button firstButton = (Button) findViewById(R.id.btn_first_page);
		firstButton.setVisibility(View.GONE);
		Button lastButton = (Button) findViewById(R.id.btn_last_page);
		lastButton.setVisibility(View.GONE);
		Button preButton = (Button) findViewById(R.id.btn_pre_page);
		preButton.setVisibility(View.GONE);
		Button goButton = (Button) findViewById(R.id.btn_go_page);
		goButton.setVisibility(View.GONE);
		Button nextButton = (Button) findViewById(R.id.btn_next_page);
		nextButton.setVisibility(View.GONE);
		
		LoadMailContentTask loadMailContentTask = new LoadMailContentTask(this);
		loadMailContentTask.execute();
	}

	public void loadMailContent() {
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(new ReadMailAdapter(this, inflater));
	}

}
