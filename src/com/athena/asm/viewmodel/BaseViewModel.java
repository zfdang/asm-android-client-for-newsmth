package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewModel {

	public interface OnViewModelChangObserver {

		public void onViewModelChange(BaseViewModel viewModel,
				String changedPropertyName, Object... params);

	}

	private List<OnViewModelChangObserver> m_changeObserverList = new ArrayList<BaseViewModel.OnViewModelChangObserver>();
	
	private boolean m_isNotificationEnabled = true;

	public synchronized void registerViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.add(observer);
	}

	public synchronized void unregisterViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.remove(observer);
	}

	public synchronized void notifyViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {

		if (!m_isNotificationEnabled) {
			return;
		}
		
		for (int i = 0; i < m_changeObserverList.size(); i++) {
			m_changeObserverList.get(i).onViewModelChange(viewModel,
					changedPropertyName, params);
		}
	}
	
	public void setChangeNotificationEnabled(boolean enabled) {
		m_isNotificationEnabled = enabled;
	}

}
