package com.athena.asm;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.athena.asm.Adapter.MailListAdapter;
import com.athena.asm.data.Mail;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadMailListTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.MailViewModel;

public class MailListActivity extends Activity implements OnClickListener, BaseViewModel.OnViewModelChangObserver {

	private LayoutInflater m_inflater;

	private MailViewModel m_viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		aSMApplication application = (aSMApplication) getApplication();
		m_viewModel = application.getMailViewModel();
	    m_viewModel.registerViewModelChangeObserver(this);

		boolean isToUpdate = m_viewModel.tryUpdateMailboxType((getIntent().getIntExtra(StringUtility.MAIL_BOX_TYPE, 0)));
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		
		if (HomeActivity.m_application.isNightTheme()) {
			((LinearLayout)titleTextView.getParent().getParent()).setBackgroundColor(getResources().getColor(R.color.body_background_night));
		}
		
		titleTextView.setText(m_viewModel.getTitleText());
		
		EditText pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setVisibility(View.GONE);

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

		if (isToUpdate) {
			LoadMailListTask loadMailListTask = new LoadMailListTask(this, m_viewModel, -1);
			loadMailListTask.execute();
		}
		else {
			reloadMailList();
		}
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver();
		
		super.onDestroy();
	}

	public void reloadMailList() {
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(new MailListAdapter(m_inflater, m_viewModel.getMailList()));
		
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
			startNumber = m_viewModel.getPrevPageStartNumber();
		} else if (view.getId() == R.id.btn_next_page) {
			startNumber = m_viewModel.getNextPageStartNumber();
		}
		LoadMailListTask loadMailListTask = new LoadMailListTask(this, m_viewModel, startNumber);
		loadMailListTask.execute();
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		
		if (changedPropertyName.equals(MailViewModel.MAILLIST_PROPERTY_NAME)) {
			reloadMailList();
		}
	}
}
