package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.athena.asm.Adapter.MailListAdapter;
import com.athena.asm.data.Mail;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadMailListTask;

public class MailListActivity extends Activity implements OnClickListener {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	public List<Mail> maillList;

	public int boxType = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();

		boxType = getIntent().getIntExtra(StringUtility.MAIL_BOX_TYPE, 0);
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		switch (boxType) {
		case 0:
			titleTextView.setText("收件箱");
			break;
		case 1:
			titleTextView.setText("发件箱");
			break;
		case 2:
			titleTextView.setText("垃圾箱");
			break;
		default:
			break;
		}
		
		EditText pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setVisibility(View.GONE);
		TextView totalPageNoTextView = (TextView) findViewById(R.id.textview_page_total_no);
		totalPageNoTextView.setVisibility(View.GONE);

		Button firstButton = (Button) findViewById(R.id.btn_first_page);
		firstButton.setOnClickListener(this);
		Button lastButton = (Button) findViewById(R.id.btn_last_page);
		lastButton.setOnClickListener(this);
		Button preButton = (Button) findViewById(R.id.btn_pre_page);
		preButton.setOnClickListener(this);
		Button goButton = (Button) findViewById(R.id.btn_go_page);
		goButton.setVisibility(View.GONE);
		Button nextButton = (Button) findViewById(R.id.btn_next_page);
		nextButton.setOnClickListener(this);

		LoadMailListTask loadMailListTask = new LoadMailListTask(this, boxType, -1);
		loadMailListTask.execute();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	public void reloadMailList() {
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(new MailListAdapter(inflater, maillList));
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable(StringUtility.MAIL, (Mail)view.getTag());
				intent.putExtras(bundle);
				intent.setClassName("com.athena.asm", "com.athena.asm.ReadMailActivity");
				startActivity(intent);
			}
		});
	}

	@Override
	public void onClick(View view) {
		int startNumber = 0;
		if (view.getId() == R.id.btn_first_page) {
			startNumber = 0;
		} else if (view.getId() == R.id.btn_last_page) {
			startNumber = -1;
		} else if (view.getId() == R.id.btn_pre_page) {
			startNumber = maillList.get(0).getNumber() - 20 + 1;
		} else if (view.getId() == R.id.btn_next_page) {
			startNumber = maillList.get(maillList.size() - 1).getNumber() + 1;
		}
		LoadMailListTask loadMailListTask = new LoadMailListTask(this, boxType, startNumber);
		loadMailListTask.execute();
	}
}
