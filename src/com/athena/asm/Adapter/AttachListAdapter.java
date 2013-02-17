package com.athena.asm.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.athena.asm.AttachUploadActivity;
import com.athena.asm.R;

public class AttachListAdapter extends BaseAdapter implements OnClickListener {

	private AttachUploadActivity activity;
	private LayoutInflater inflater;

	public AttachListAdapter(AttachUploadActivity activity, LayoutInflater inflater) {
		this.activity = activity;
		this.inflater = inflater;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		}
		else {
			layout = inflater.inflate(R.layout.attach_list_item, null);
		}
		
		TextView titleTextView = (TextView) layout.findViewById(R.id.attach_title);
		titleTextView.setText(activity.m_attachArrayList.get(position).getName());
		Button deleteButton = (Button) layout.findViewById(R.id.btn_delete_attach);
		deleteButton.setOnClickListener(this);
		deleteButton.setTag(position);
		
		return layout;
	}

	@Override
	public int getCount() {
		return activity.m_attachArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return activity.m_attachArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		activity.m_attachArrayList.remove(position);
		
		this.notifyDataSetChanged();
	}
}
