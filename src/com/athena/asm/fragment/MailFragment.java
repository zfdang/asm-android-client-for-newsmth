package com.athena.asm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.MailAdapter;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;

public class MailFragment extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver {

	private HomeViewModel m_viewModel;

	private LayoutInflater m_inflater;

	private ListView m_listView;

	private boolean m_isLoaded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_inflater = inflater;
		View layout = m_inflater.inflate(R.layout.mail, null);
		m_listView = (ListView) layout.findViewById(R.id.mail_list);

		aSMApplication application = (aSMApplication) getActivity()
				.getApplication();
		m_viewModel = application.getHomeViewModel();
		m_viewModel.registerViewModelChangeObserver(this);

		m_isLoaded = false;

		return m_listView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (m_viewModel.getCurrentTab() != null
				&& m_viewModel.getCurrentTab().equals(StringUtility.TAB_MAIL)) {
			reloadMail();
		}
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);
		super.onDestroy();
	}

	public void reloadMail() {
		if (m_viewModel.isLogined()) {
			if (m_viewModel.getMailBox() == null) {
				LoadMailTask loadMailTask = new LoadMailTask(getActivity(),
						m_viewModel);
				loadMailTask.execute();
			} else {
				m_isLoaded = true;
				m_listView.setAdapter(new MailAdapter(m_inflater, m_viewModel
						.getMailBox()));

				m_listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							final int position, long id) {
						if (position == 0 || position == 1 || position == 2) {
							final int boxType = position;
							Intent intent = new Intent();
							intent.putExtra(StringUtility.MAIL_BOX_TYPE,
									boxType);
							intent.setClassName("com.athena.asm",
									"com.athena.asm.MailListActivity");
							startActivity(intent);
						} else if (position == 3) {
							Intent intent = new Intent();
							intent.setClassName("com.athena.asm",
									"com.athena.asm.WritePostActivity");
							intent.putExtra(StringUtility.URL,
									"http://www.newsmth.net/bbspstmail.php");
							intent.putExtra(StringUtility.WRITE_TYPE, 1);
							intent.putExtra(StringUtility.IS_REPLY, false);
							startActivity(intent);
						}
					}
				});
			}
		} else {
			Toast.makeText(getActivity().getApplicationContext(), "请登陆后再使用.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(HomeViewModel.MAILBOX_PROPERTY_NAME)) {
			reloadMail();
		} else if (changedPropertyName
				.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
			if (!m_isLoaded
					&& m_viewModel.getCurrentTab() != null
					&& m_viewModel.getCurrentTab().equals(
							StringUtility.TAB_MAIL)) {
				reloadMail();
			}
		}
	}
}
