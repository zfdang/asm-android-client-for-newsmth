package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.athena.asm.viewmodel.BaseViewModel.OnViewModelChangObserver;

public abstract class BaseViewModel {

	public interface OnViewModelChangObserver {

		public void onViewModelChange(BaseViewModel viewModel,
				String changedPropertyName, Object... params);

	}

	private List<OnViewModelChangObserver> m_changeObserverList = new ArrayList<BaseViewModel.OnViewModelChangObserver>();

	public void registerViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.add(observer);
	}

	public void unregisterViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.remove(observer);
	}

	public void notifyViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		for (Iterator<OnViewModelChangObserver> iterator = m_changeObserverList
				.iterator(); iterator.hasNext();) {
			OnViewModelChangObserver changeObserver = iterator.next();
			changeObserver.onViewModelChange(viewModel, changedPropertyName,
					params);

		}
	}

}
