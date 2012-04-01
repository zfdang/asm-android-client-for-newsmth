package com.athena.asm.viewmodel;

public abstract class BaseViewModel {
	
	public interface OnViewModelChangObserver {
		
		public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params);
		
	}
	
	private OnViewModelChangObserver m_changeObserver;
	
	public void registerViewModelChangeObserver(OnViewModelChangObserver observer) {
		m_changeObserver = observer;
	}
	
	public void unregisterViewModelChangeObserver() {
		m_changeObserver = null;
	}
	
	public void notifyViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params) {
		if (m_changeObserver != null) {
			m_changeObserver.onViewModelChange(viewModel, changedPropertyName, params);
		}
	}

}
