package com.athena.asm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> mData = null;
    private ArrayList<String> filteredData = null;

    public AutoCompleteAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);

        // original data
        mData = new ArrayList<String>();
        mData.addAll(objects);
        Collections.sort(mData);

        // filtered data
        filteredData = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public String getItem(int index) {
        return filteredData.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                // clear results
                filteredData.clear();
                if(constraint != null) {
                    String key = constraint.toString();
                    Pattern keyPattern = Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE);
                    // Log.d("AutoCompleteAdapter", key);
                    Iterator<String> itr = mData.iterator();
                    while(itr.hasNext()){
                        String current = (String) itr.next();
                        if(keyPattern.matcher(current).find())
                        {
                            filteredData.add(current);
                            // Log.d("AutoCompleteAdapter", "matched with " + current);
                        }
                    }
                    // Now assign the values and count to the FilterResults object
                    filterResults.values = filteredData;
                    filterResults.count = filteredData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }
}