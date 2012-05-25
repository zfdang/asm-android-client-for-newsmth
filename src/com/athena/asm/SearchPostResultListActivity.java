package com.athena.asm;

import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.athena.asm.Adapter.SearchPostResultListAdapter;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

public class SearchPostResultListActivity extends SherlockActivity {

	public SmthSupport m_smthSupport;

	private LayoutInflater m_inflater;

	public List<Subject> m_subjectList;

	public int m_boardType = 1;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(HomeActivity.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_post_result_list);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_smthSupport = SmthSupport.getInstance();
		
		m_subjectList = (List<Subject>) getIntent().getSerializableExtra(StringUtility.SUBJECT_LIST);
		
		if (m_subjectList.size() > 0) {
			setTitle("搜索结果");
		}
		else {
			setTitle("没有搜到符合的结果");
		}
		
		ListView listView = (ListView) findViewById(R.id.search_result_subject_list);
		listView.setAdapter(new SearchPostResultListAdapter(m_inflater, m_subjectList));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable(StringUtility.SUBJECT, (Subject)view.getTag());
				bundle.putInt(StringUtility.BOARD_TYPE, m_boardType);
				intent.putExtras(bundle);
				intent.setClassName("com.athena.asm", "com.athena.asm.PostListActivity");
				startActivity(intent);
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
}
