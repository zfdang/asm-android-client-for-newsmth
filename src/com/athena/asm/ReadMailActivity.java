package com.athena.asm;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.athena.asm.Adapter.ReadMailAdapter;
import com.athena.asm.data.Mail;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadMailContentTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.MailViewModel;

public class ReadMailActivity extends SherlockActivity implements BaseViewModel.OnViewModelChangObserver {

	public SmthSupport m_smthSupport;

	private LayoutInflater m_inflater;

	private MailViewModel m_viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_list);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_smthSupport = SmthSupport.getInstance();
		
		aSMApplication application = (aSMApplication) getApplication();
		m_viewModel = application.getMailViewModel();
	    m_viewModel.registerViewModelChangeObserver(this);

		boolean isNewMail = m_viewModel.tryUpdateCurrentMail((Mail) getIntent().getSerializableExtra(
				StringUtility.MAIL));

		setTitle(m_viewModel.getCurrentMailTitle());

		EditText pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setVisibility(View.GONE);

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
		
		if (isNewMail) {
			LoadMailContentTask loadMailContentTask = new LoadMailContentTask(this, m_viewModel);
			loadMailContentTask.execute();
		}
		else {
			loadMailContent();
		}
			
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);
		
		super.onDestroy();
	}

	public void loadMailContent() {
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(new ReadMailAdapter(this, m_viewModel.getCurrentMail(), m_inflater));
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(MailViewModel.CURRENT_MAIL_CONTENT_PROPERTY_NAME)) {
			loadMailContent();
		}
	}

}
