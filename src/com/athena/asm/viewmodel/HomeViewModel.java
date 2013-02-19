package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.athena.asm.data.Board;
import com.athena.asm.data.MailBox;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;

public class HomeViewModel extends BaseViewModel {
	
	private static final String GUEST_ID = "guest";

	private List<String> m_guidanceSectionNames = null;
	private List<List<Subject>> m_guidanceSectionDetails = null;
	private List<Board> m_favList = null;
	private MailBox m_mailBox = null;
	private ArrayList<Board> m_categoryList = null;
	private List<String> m_boardFullStrings = null;
	private HashMap<String, Board> m_boardHashMap = null;

	private Profile m_currentProfile = null;
	
	private boolean m_isLogined = false;
    private boolean m_isGuestLoggedin = false;
    private String m_loginUserID = GUEST_ID;
    
    private String m_currentTab = null;

	private SmthSupport m_smthSupport;
	
	public static final String CURRENTTAB_PROPERTY_NAME = "CurrentTab";
	public static final String GUIDANCE_PROPERTY_NAME = "Guidance";
	public static final String CATEGORYLIST_PROPERTY_NAME = "CategoryList";
	public static final String FAVLIST_PROPERTY_NAME = "FavList";
	public static final String MAILBOX_PROPERTY_NAME = "Mailbox";
	public static final String PROFILE_PROPERTY_NAME = "Profile";
	public static final String LOGIN_PROPERTY_NAME = "Login";
	
	public HomeViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public boolean isLogined() {
		return m_isLogined;
	}
	
	public void setLoggedin(boolean isUserLoggedin) {
		m_isLogined = isUserLoggedin;
	}
	
	public boolean isGuestLogined() {
		return m_isGuestLoggedin;
	}
	
	public void setGuestLogined(boolean isGuestLoggedin) {
		m_isGuestLoggedin = isGuestLoggedin;
	}
	
	public String getLoginUserID() {
		return m_loginUserID;
	}
	
	public void updateLoginStatus() {
		m_isLogined = m_smthSupport.getLoginStatus();
		if (m_isLogined) {
			m_loginUserID = m_smthSupport.getUserid();
		}
	}
	
	public List<String> getGuidanceSectionNames() {
		return m_guidanceSectionNames;
	}
	
	public void setGuidanceSectionNames(List<String> guidanceSectionNames) {
		m_guidanceSectionNames = guidanceSectionNames;
	}
	
	public List<List<Subject>> getGuidanceSectionDetails() {
		return m_guidanceSectionDetails;
	}
	
	public void setGuidanceSectionDetails(List<List<Subject>> guidanceSectionDetails) {
		m_guidanceSectionDetails = guidanceSectionDetails;
	}
	
	public List<Board> getFavList() {
		return m_favList;
	}
	
	public void setFavList(List<Board> favList) {
		m_favList = favList;
	}
	
	public MailBox getMailBox() {
		return m_mailBox;
	}
	
	public void setMailbox(MailBox mailbox) {
		m_mailBox = mailbox;
	}
	
	public ArrayList<Board> getCategoryList() {
		return m_categoryList;
	}
	
	public void setCategoryList(ArrayList<Board> categoryList) {
		m_categoryList = categoryList;
	}
	
	public List<String> getBoardFullStrings() {
		return m_boardFullStrings;
	}
	
	public HashMap<String, Board> getBoardHashMap() {
		return m_boardHashMap;
	}
	
	public Profile getCurrentProfile() {
		return m_currentProfile;
	}
	
	public void setCurrentProfile(Profile currentProfile) {
		m_currentProfile = currentProfile;
	}
	
	public String getCurrentTab() {
		return m_currentTab;
	}
	
	public void setCurrentTab(String currentTab) {
		m_currentTab = currentTab;
		
		notifyViewModelChange(this, CURRENTTAB_PROPERTY_NAME);
	}
	
	public int login(String userName, String password) {
		m_smthSupport.setUserid(userName);
		m_smthSupport.setPasswd(password);
		return m_smthSupport.login();
	}
	
	public void logout() {
		m_smthSupport.destory();
		
		m_currentTab = null;
		m_loginUserID = GUEST_ID;
		m_isLogined = false;
		m_isGuestLoggedin = false;
		m_currentProfile = null;
		m_favList = null;
		m_mailBox = null;
	}
	
	public void restorSmthSupport() {
		m_smthSupport.restore();
	}
	
	@SuppressWarnings("unchecked")
	public void updateGuidance() {
		Object[] guidance = m_smthSupport.getGuidance();
		setGuidanceSectionNames((List<String>) guidance[0]);
		setGuidanceSectionDetails((List<List<Subject>>) guidance[1]);
	}
	
	
	public ArrayList<Board> updateFavList(ArrayList<Board> realFavList) {
		if (realFavList == null) {
			realFavList = new ArrayList<Board>();
		}
		else{
			realFavList.clear();
		}
		m_smthSupport.getFavorite("0", realFavList, 0);
		
		ArrayList<Board> favList = new ArrayList<Board>();
		favList.addAll(realFavList);

		Board board = new Board();
		board.setDirectory(true);
		board.setDirectoryName("最近访问版面");
		board.setCategoryName("目录");
		//board.setChildBoards(new ArrayList<Board>(application.getRecentBoards()));
		favList.add(board);
		
		setFavList(favList);
		return favList;
	}
	
	public void updateCategoryList() {
		ArrayList<Board> categoryList = new ArrayList<Board>();
		m_smthSupport.getCategory("TOP", categoryList, false);
		setCategoryList(categoryList);
	}
	
	public void updateMailbox() {
		setMailbox(m_smthSupport.getMailBoxInfo());
	}
	
	public String checkNewMessage() {
		String result = null;
		boolean isNewMail = m_smthSupport.checkNewMail();
		String replyOrAtResult = m_smthSupport.checkNewReplyOrAt();
		
		if (isNewMail) {
			result = "新信件";
		} else if (replyOrAtResult != null) {
			result = replyOrAtResult;
		}
		
		if (m_mailBox != null) {
			m_mailBox.setHavingNewMail(isNewMail);
		}

		return result;
	}
	
	public Profile getProfile(String userID) {
		Profile profile = m_smthSupport.getProfile(userID);
		if (userID.equals(m_loginUserID)) {
			setCurrentProfile(profile);
		}
		
		return profile;
	}
	
	public void updateBoardInfo() {
		m_boardFullStrings = new ArrayList<String>();
		m_boardHashMap = new HashMap<String, Board>();
		readBoadInfo(m_categoryList);
	}
	
	private void readBoadInfo(List<Board> boards) {
		for (Iterator<Board> iterator = boards.iterator(); iterator.hasNext();) {
			Board board = (Board) iterator.next();
			if (board != null) {
				if (board.getEngName() != null) {
					if (!m_boardFullStrings.contains(board.getEngName())) {
						m_boardFullStrings.add(board.getEngName());
					}
					m_boardHashMap.put(board.getEngName().toLowerCase(), board);
				}
				if (board.getChsName() != null) {
					if (!m_boardFullStrings.contains(board.getChsName())) {
						m_boardFullStrings.add(board.getChsName());
					}
					m_boardHashMap.put(board.getChsName(), board);
				}
			}
			if (board.getChildBoards() != null) {
				readBoadInfo(board.getChildBoards());
			}
		}
	}
	
	public void clear() {
		m_guidanceSectionNames = null;
		m_guidanceSectionDetails = null;
		m_favList = null;
		m_mailBox = null;
		m_categoryList = null;
		m_currentProfile = null;
	}
	
	public void notifyGuidanceChanged() {
		notifyViewModelChange(this, GUIDANCE_PROPERTY_NAME);
	}
	
	public void notifyFavListChanged() {
		notifyViewModelChange(this, FAVLIST_PROPERTY_NAME);
	}
	
	public void notifyCategoryChanged() {
		notifyViewModelChange(this, CATEGORYLIST_PROPERTY_NAME);
	}
	
	public void notifyMailboxChanged() {
		notifyViewModelChange(this, MAILBOX_PROPERTY_NAME);
	}
	
	public void notifyProfileChanged(Profile profile) {
		notifyViewModelChange(this, PROFILE_PROPERTY_NAME, profile);
	}
	
	public void notifyLoginChanged(int iLoginResult) {
		notifyViewModelChange(this, LOGIN_PROPERTY_NAME, iLoginResult);
	}
	
}
