package com.athena.asm.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.athena.asm.ActivityFragmentTargets;
import com.athena.asm.OnOpenActivityFragmentListener;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Profile;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadProfileTask;
import com.athena.asm.util.vt100.Vt100TerminalModel;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;

public class ProfileFragment extends SherlockFragment implements BaseViewModel.OnViewModelChangObserver {

    private HomeViewModel m_viewModel;

    private LayoutInflater m_inflater;

    private View m_layout;

    private Profile m_currentProfile;

    private boolean m_isLoaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_inflater = inflater;
        m_layout = m_inflater.inflate(R.layout.profile, null);

        aSMApplication application = (aSMApplication) getActivity().getApplication();
        m_viewModel = application.getHomeViewModel();
        m_viewModel.registerViewModelChangeObserver(this);

        m_currentProfile = null;
        m_isLoaded = false;

        return m_layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (m_viewModel.getCurrentTab() != null && m_viewModel.getCurrentTab().equals(StringUtility.TAB_PROFILE)) {
            reloadProfile();
        }
    }

    @Override
    public void onDestroy() {
        m_viewModel.unregisterViewModelChangeObserver(this);
        super.onDestroy();
    }

    public void reloadProfile() {
        if (m_currentProfile == null) {
            if (m_viewModel.m_isLoadingInProgress)
                return;
            LoadProfileTask loadProfileTask = new LoadProfileTask(getActivity(), m_viewModel,
                    m_viewModel.getLoginUserID());
            loadProfileTask.execute();
        } else {
            ImageButton searchButton = (ImageButton) m_layout.findViewById(R.id.btn_search);
            searchButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    RelativeLayout relativeLayout = (RelativeLayout) v.getParent();
                    EditText searchEditText = (EditText) relativeLayout.findViewById(R.id.search_edit);

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                            SherlockActivity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                    String idString = searchEditText.getText().toString().trim();
                    if (idString.length() > 0) {
                        LoadProfileTask profileTask = new LoadProfileTask(getActivity(), m_viewModel, idString);
                        profileTask.execute();
                    }

                }
            });

            Button msgButton = (Button) m_layout.findViewById(R.id.btn_send_mail);
            msgButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    OnOpenActivityFragmentListener onOpenActivityFragmentListener = null;
                    Activity parentActivity = getSherlockActivity();
                    if (parentActivity instanceof OnOpenActivityFragmentListener) {
                        onOpenActivityFragmentListener = (OnOpenActivityFragmentListener) parentActivity;
                    }
                    if (onOpenActivityFragmentListener != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(StringUtility.URL, "http://www.newsmth.net/bbspstmail.php");
                        bundle.putSerializable(StringUtility.WRITE_TYPE, 1);
                        bundle.putSerializable(StringUtility.IS_REPLY, false);
                        bundle.putSerializable(StringUtility.USERID, m_currentProfile.getUserID());
                        onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.WRITE_POST,
                                bundle);
                    }
                }
            });

            TextView userIDTextView = (TextView) m_layout.findViewById(R.id.profile_userid);
            userIDTextView.setText(m_currentProfile.getUserIDNickName());
            userIDTextView.requestFocus();

            TextView userScoreTextView = (TextView) m_layout.findViewById(R.id.profile_user_score);
            if (m_currentProfile.getScore() != 0) {
                userScoreTextView.setVisibility(View.VISIBLE);
                userScoreTextView.setText("积分:" + m_currentProfile.getScore());
            } else {
                userScoreTextView.setVisibility(View.GONE);
            }

            TextView userIpTextView = (TextView) m_layout.findViewById(R.id.profile_user_ip);
            userIpTextView.setText("来自:" + m_currentProfile.getIp());

            TextView descTextView = (TextView) m_layout.findViewById(R.id.profile_user_desc);
    		Vt100TerminalModel.handleContent(m_currentProfile.getDescription(), descTextView);		                        
//            descTextView.setText(Html.fromHtml(m_currentProfile.getDescription()));

            TextView aliveTextView = (TextView) m_layout.findViewById(R.id.profile_aliveness);
            aliveTextView.setText(m_currentProfile.getAliveness() + "");
            TextView loginedTimeTextView = (TextView) m_layout.findViewById(R.id.profile_login_times);
            loginedTimeTextView.setText(m_currentProfile.getLoginTime() + "");
            TextView postNoTextView = (TextView) m_layout.findViewById(R.id.profile_post_number);
            postNoTextView.setText(m_currentProfile.getPostNumber() + "");
            TextView onlineTextView = (TextView) m_layout.findViewById(R.id.profile_online_status);
            switch (m_currentProfile.getOnlineStatus()) {
            case 0:
                onlineTextView.setText("离线");
                break;
            case 1:
                onlineTextView.setText("不明");
                break;
            case 2:
                onlineTextView.setText("在线");
                break;

            default:
                break;
            }

            if (aSMApplication.getCurrentApplication().isNightTheme()) {
                userIDTextView.setTextColor(m_layout.getResources().getColor(R.color.blue_text_night));
                searchButton.setImageResource(R.drawable.search);
            } else {
                searchButton.setImageResource(R.drawable.search_inverse);
            }

        }
    }

    @Override
    public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object... params) {
        if (changedPropertyName.equals(HomeViewModel.PROFILE_PROPERTY_NAME)) {
            m_currentProfile = (Profile) params[0];

            reloadProfile();
        } else if (changedPropertyName.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
            if (!m_isLoaded && m_viewModel.getCurrentTab() != null
                    && m_viewModel.getCurrentTab().equals(StringUtility.TAB_PROFILE)) {
                reloadProfile();
            }
        }
    }
}
