package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.athena.asm.Adapter.SearchPostResultListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

public class SearchPostResultListActivity extends Activity {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	public List<Subject> subjectList;

	public int boardType = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_post_result_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();
		
		subjectList = (List<Subject>) getIntent().getSerializableExtra(StringUtility.SUBJECT_LIST);

		TextView titleTextView = (TextView) findViewById(R.id.title);
		if (subjectList.size() > 0) {
			titleTextView.setText("搜索结果");
		}
		else {
			titleTextView.setText("没有搜到符合的结果");
		}
		
		ListView listView = (ListView) findViewById(R.id.search_result_subject_list);
		listView.setAdapter(new SearchPostResultListAdapter(this, inflater, subjectList));
	}
}
