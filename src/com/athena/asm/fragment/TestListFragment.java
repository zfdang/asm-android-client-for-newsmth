package com.athena.asm.fragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class TestListFragment extends SherlockListFragment {
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        String[] titles = {"Title 1", "Title 2", "Title 3", "Title 4", "Title 5"};
        
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, titles));
    }
}
