package com.athena.asm;

import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.viewmodel.SearchPostViewModel;

public class SearchPostActivity extends SherlockActivity implements
		OnClickListener {
	public SmthSupport m_smthSupport;

	EditText m_titleEditText;
	EditText m_title2EditText;
	EditText m_title3EditText;
	EditText m_useridEditText;

    EditText m_dtEditText;

	CheckBox m_mgCheckBox;
	CheckBox m_agCheckBox;
	CheckBox m_ogCheckBox;

	Button m_startSearchButton;

	private SearchPostViewModel m_viewModel;

	@SuppressWarnings("deprecation")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_post);

		m_smthSupport = SmthSupport.getInstance();

		m_viewModel = (SearchPostViewModel) getLastNonConfigurationInstance();
		if (m_viewModel == null) {
			m_viewModel = new SearchPostViewModel();
		}

		m_viewModel.setBoardName(getIntent()
				.getStringExtra(StringUtility.BOARD));
		m_viewModel.setBoardID(getIntent().getStringExtra(StringUtility.BID));

		setTitle(m_viewModel.getTitleText());

		m_titleEditText = (EditText) findViewById(R.id.edittext_title);
		// turn on IME automatically
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }, 300);

		m_title2EditText = (EditText) findViewById(R.id.edittext_title2);
		m_title3EditText = (EditText) findViewById(R.id.edittext_title3);
		m_useridEditText = (EditText) findViewById(R.id.edittext_userid);
		m_dtEditText = (EditText) findViewById(R.id.edittext_dt);

		m_mgCheckBox = (CheckBox) findViewById(R.id.checkbox_mg);
		m_agCheckBox = (CheckBox) findViewById(R.id.checkbox_ag);
		m_ogCheckBox = (CheckBox) findViewById(R.id.checkbox_og);

		m_startSearchButton = (Button) findViewById(R.id.btn_start_post_search);
		m_startSearchButton.setOnClickListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		updateViewModel();
		return m_viewModel;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_start_post_search) {
			updateViewModel();
			new SearchTask().execute();
		}
	}
	
	public static final int MENU_SEARCH = Menu.FIRST;

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;
        menu.add(0, MENU_SEARCH, Menu.NONE, "搜索").setIcon(isLight ? R.drawable.search_inverse : R.drawable.search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	    return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SEARCH:
            m_startSearchButton.performClick();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

	public void parseSearchResult(List<Subject> subjects) {
		Intent intent = new Intent();
		intent.setClassName("com.athena.asm",
				"com.athena.asm.SearchPostResultListActivity");
		Bundle bundle = new Bundle();
		bundle.putSerializable(StringUtility.SUBJECT_LIST,
				(Serializable) subjects);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	class SearchTask extends AsyncTask<String, Integer, String> {

		private List<Subject> m_subjects;
		
		@Override
		protected String doInBackground(String... arg0) {
			m_subjects = m_viewModel.searchSubject();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			parseSearchResult(m_subjects);
		}
	}

	private void updateViewModel() {
		m_viewModel.setTitle(m_titleEditText.getText().toString().trim());
		m_viewModel.setTitle2(m_title2EditText.getText().toString().trim());
		m_viewModel.setTitle3(m_title3EditText.getText().toString().trim());
		m_viewModel.setUserId(m_useridEditText.getText().toString().trim());
		m_viewModel.setDays(m_dtEditText.getText().toString().trim());
		m_viewModel.setMgFlag(m_mgCheckBox.isChecked());
		m_viewModel.setAgFlag(m_agCheckBox.isChecked());
		m_viewModel.setOgFlag(m_ogCheckBox.isChecked());
	}
}
