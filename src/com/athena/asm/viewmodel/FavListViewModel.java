package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.List;

import com.athena.asm.data.Board;
import com.athena.asm.util.SmthSupport;

public class FavListViewModel extends BaseViewModel {
	
	private List<Board> m_favList = null;
	
	private SmthSupport m_smthSupport;
	
	public static final String FAVLIST_PROPERTY_NAME = "FavList";
	
	public FavListViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public void clear() {
		setFavList(null);
	}

	public List<Board> getFavList() {
		return m_favList;
	}
	
	public void setFavList(List<Board> favList) {
		m_favList = favList;
	}
	
	public ArrayList<Board> updateFavList(ArrayList<Board> realFavList) {
		if (realFavList == null) {
			realFavList = new ArrayList<Board>();
			m_smthSupport.getFavorite("0", realFavList, 0);
		}
		
		ArrayList<Board> favList = new ArrayList<Board>();
		Board board = new Board();
		board.setDirectory(true);
		board.setDirectoryName("最近访问版面");
		board.setCategoryName("目录");
		//board.setChildBoards(new ArrayList<Board>(application.getRecentBoards()));
		favList.add(board);
		favList.addAll(realFavList);
		setFavList(favList);
		
		return realFavList;
	}
	
	public void notifyFavListChanged() {
		notifyViewModelChange(this, FAVLIST_PROPERTY_NAME);
	}
}
