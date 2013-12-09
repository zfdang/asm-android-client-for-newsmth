package com.athena.asm.Adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Board;

public class CategoryListAdapter extends ArrayAdapter<Board> implements SectionIndexer{


    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
    private LayoutInflater m_inflater;
    private List<Board> m_boards;


    public CategoryListAdapter(Context context, int textViewResourceId, List<Board> data, LayoutInflater inflater) {
        super(context, textViewResourceId, data);
        m_boards = data;
        m_inflater = inflater;

        alphaIndexer = new HashMap<String, Integer>();
        for (int i = 0; i < data.size(); i++)
        {
            String s = data.get(i).getEngName().substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s))
                alphaIndexer.put(s, i);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = sectionList.toArray(new String[sectionList.size()]);
    }

	public class ViewHolder {
		public TextView categoryNameTextView;
		public TextView moderatorIDTextView;
		public TextView boardNameTextView;
		public Board board;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Board board = m_boards.get(position);
		View layout = null;
		ViewHolder holder;
		if (convertView != null) {
			layout = convertView;
			holder = (ViewHolder)layout.getTag();
		} else {
			layout = m_inflater.inflate(R.layout.category_list_item, null);
			holder = new ViewHolder();
			holder.categoryNameTextView = (TextView) layout.findViewById(R.id.CategoryName);
			holder.moderatorIDTextView = (TextView) layout.findViewById(R.id.ModeratorID);
			holder.boardNameTextView = (TextView) layout.findViewById(R.id.BoardName);
			
			holder.boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceSecondFontSize());

			if (aSMApplication.getCurrentApplication().isNightTheme()) {
				holder.categoryNameTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
				holder.moderatorIDTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
				holder.boardNameTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
			}
			layout.setTag(holder);
		}
		
		holder.board = board;
		holder.categoryNameTextView.setText(board.getCategoryName());
		holder.moderatorIDTextView.setText(board.getModerator());
		holder.boardNameTextView.setText("[" + board.getEngName() + "]"
				+ board.getChsName());
		
		return layout;
	}

	@Override
	public int getCount() {
		return m_boards.size();
	}

	@Override
	public Board getItem(int position) {
		return m_boards.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public int getPositionForSection(int section) {
        // TODO Auto-generated method stub
        return alphaIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object[] getSections() {
        // TODO Auto-generated method stub
        return sections;
    }
}
