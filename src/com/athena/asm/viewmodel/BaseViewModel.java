package com.athena.asm.viewmodel;

public abstract class BaseViewModel {
	
	public interface OnViewModelChangObserver {
		
		public void OnViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params);
		
	}
	
	private OnViewModelChangObserver m_changeObserver;
	
	public void RegisterViewModelChangeObserver(OnViewModelChangObserver observer) {
		m_changeObserver = observer;
	}
	
	public void UnregisterViewModelChangeObserver() {
		m_changeObserver = null;
	}
	
	public void NotifyViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params) {
		if (m_changeObserver != null) {
			m_changeObserver.OnViewModelChange(viewModel, changedPropertyName, params);
		}
	}

}
