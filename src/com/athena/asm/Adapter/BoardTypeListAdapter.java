package com.athena.asm.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;

public class BoardTypeListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private int boardType;
	private String[] items = { "同主题", "普通模式", "文摘区", "保留区" };

	public BoardTypeListAdapter(int boardType, LayoutInflater inflater) {
		this.boardType = boardType;
		this.inflater = inflater;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		}
		else {
			layout = inflater.inflate(android.R.layout.simple_list_item_1, null);
		}
		
		TextView textView=(TextView) layout.findViewById(android.R.id.text1);
		textView.setText(items[position]);
		if (position == boardType) {
			textView.setTextColor(Color.GRAY);
		}
		else {
			boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;
			if (isLight) {
				textView.setTextColor(Color.BLACK);
			} else {
				textView.setTextColor(Color.WHITE);
			}
			
		}
		
		return layout;
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public boolean isEnabled(int position) {
		if (position == boardType) {
			return false;
		}
		else {
			return true;
		}
	}

}
