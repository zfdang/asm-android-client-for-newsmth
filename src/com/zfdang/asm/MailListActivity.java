package com.zfdang.asm;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zfdang.asm.Adapter.MailListAdapter;
import com.zfdang.asm.data.Mail;
import com.zfdang.asm.fragment.SubjectListFragment;
import com.zfdang.asm.util.StringUtility;
import com.zfdang.asm.util.task.LoadMailListTask;
import com.zfdang.asm.viewmodel.BaseViewModel;
import com.zfdang.asm.viewmodel.MailViewModel;
import com.zfdang.asm.R;

public class MailListActivity extends SherlockActivity implements
		OnClickListener, BaseViewModel.OnViewModelChangObserver {

	private LayoutInflater m_inflater;

	private MailViewModel m_viewModel;

	private MailListAdapter m_listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_list);

		setRequestedOrientation(aSMApplication.ORIENTATION);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		aSMApplication application = (aSMApplication) getApplication();
		m_viewModel = application.getMailViewModel();
		m_viewModel.registerViewModelChangeObserver(this);

		boolean isToUpdate = m_viewModel.tryUpdateMailboxType((getIntent()
				.getIntExtra(StringUtility.MAIL_BOX_TYPE, 0)));

		setTitle(m_viewModel.getTitleText());

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
			LoadMailListTask loadMailListTask = new LoadMailListTask(this,
					m_viewModel, -1);
			loadMailListTask.execute();
		} else {
			reloadMailList();
		}

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
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

	public void reloadMailList() {
		if (m_viewModel.getMailList() != null) {
			ListView listView = (ListView) findViewById(R.id.post_list);
			m_listAdapter = new MailListAdapter(m_inflater,
					m_viewModel.getMailList(), m_viewModel.getMailboxType());
			listView.setAdapter(m_listAdapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					if (m_viewModel.getMailboxType() < 3) {
						m_viewModel.setMailRead(m_viewModel.getMailList()
								.size() - position - 1);
					} else {
						m_viewModel.setMailRead(position);
					}

					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable(StringUtility.MAIL,
							(Mail) view.getTag());
					intent.putExtras(bundle);
					if (m_viewModel.getMailboxType() < 3) {
						intent.setClassName("com.zfdang.asm",
								"com.zfdang.asm.ReadMailActivity");
					} else {
						intent.putExtra(StringUtility.BOARD_TYPE,
								SubjectListFragment.BOARD_TYPE_NORMAL);
						intent.setClassName("com.zfdang.asm", PostListActivity.class.getName());
					}
					startActivityForResult(intent, 0);
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		m_listAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View view) {
		int startNumber = 0;
		if (view.getId() == R.id.btn_first_page) {
			startNumber = m_viewModel.getFirstPageStartNumber();
		} else if (view.getId() == R.id.btn_last_page) {
			startNumber = m_viewModel.getLastPageStartNumber();
		} else if (view.getId() == R.id.btn_pre_page) {
			startNumber = m_viewModel.getPrevPageStartNumber();
		} else if (view.getId() == R.id.btn_next_page) {
			startNumber = m_viewModel.getNextPageStartNumber();
		}
		LoadMailListTask loadMailListTask = new LoadMailListTask(this,
				m_viewModel, startNumber);
		loadMailListTask.execute();
	}

	public static final int REFRESH_MAILLIST = Menu.FIRST;
	public static final int MARK_ALL_READ = Menu.FIRST + 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;

		menu.add(0, REFRESH_MAILLIST, Menu.NONE, "刷新")
				.setIcon(
						isLight ? R.drawable.refresh_inverse
								: R.drawable.refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		if (m_viewModel.getMailboxType() > 3) {
			menu.add(0, MARK_ALL_READ, Menu.NONE, "全部已读").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
		case REFRESH_MAILLIST:
			m_viewModel.setMailList(null);
			LoadMailListTask loadMailListTask = new LoadMailListTask(this,
					m_viewModel, -1);
			loadMailListTask.execute();
			break;
		case MARK_ALL_READ:
			Toast.makeText(this, "正在标记已读，结束后返回上一级",
					Toast.LENGTH_SHORT).show();
			Thread th = new Thread() {
				@Override
				public void run() {
					m_viewModel.markAllMessageRead();
					m_viewModel.setAllMailRead();
					finish();
				}
			};
			th.start();
			break;
		}

		return true;
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {

		if (changedPropertyName.equals(MailViewModel.MAILLIST_PROPERTY_NAME)) {
			reloadMailList();
		}
	}
}
