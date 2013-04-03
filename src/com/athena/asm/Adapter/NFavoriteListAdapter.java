package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.FavoriteListAdapter.ChildViewHolder;
import com.athena.asm.data.Board;

public class NFavoriteListAdapter extends BaseAdapter {
    private LayoutInflater m_inflater;
    private List<String> m_directories;
    private List<List<Board>> m_boards;
    private List<Board> m_favorites;

    public NFavoriteListAdapter(LayoutInflater m_inflater, List<String> m_directories, List<List<Board>> m_boards) {
        super();
        this.m_inflater = m_inflater;
        this.m_directories = m_directories;
        this.m_boards = m_boards;
        this.m_favorites = this.m_boards.get(0);
    }

    public List<Board> getFavoriteBoards() {
        return m_favorites;
    }
    
    public boolean moveItem(int from, int to) {
        Board board = m_favorites.get(from);
        m_favorites.remove(board);
        m_favorites.add(to, board);
        notifyDataSetChanged();

        // TODO: save ordered list to file
        return true;
    }

    @Override
    public int getCount() {
        return m_favorites.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Board board = m_favorites.get(position);

        ChildViewHolder holder;
        if (convertView == null) {
            convertView = m_inflater.inflate(R.layout.favorite_list_item, null);

            holder = new ChildViewHolder();
            holder.categoryNameTextView = (TextView) convertView.findViewById(R.id.CategoryName);
            holder.moderatorIDTextView = (TextView) convertView.findViewById(R.id.ModeratorID);
            holder.boardNameTextView = (TextView) convertView.findViewById(R.id.BoardName);
            convertView.setTag(R.id.tag_first, holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag(R.id.tag_first);
        }
        holder.categoryNameTextView.setText(board.getCategoryName());
        holder.moderatorIDTextView.setText(board.getModerator());
        holder.boardNameTextView.setText("[" + board.getEngName() + "]" + board.getChsName());
        holder.boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication()
                .getGuidanceSecondFontSize());

        convertView.setTag(R.id.tag_second, board);

        if (aSMApplication.getCurrentApplication().isNightTheme()) {
            holder.categoryNameTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
            holder.moderatorIDTextView.setTextColor(convertView.getResources().getColor(R.color.blue_text_night));
            holder.boardNameTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
        }
        return convertView;
    }

}
