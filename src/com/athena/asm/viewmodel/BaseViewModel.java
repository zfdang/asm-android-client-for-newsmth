package com.athena.asm.viewmodel;

public abstract class BaseViewModel {
	
	public interface OnViewModelChangObserver {
		
		public void OnViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params);
		
	}
	
	protected OnViewModelChangObserver m_changeObserver;
	
	public void RegisterViewModelChangeObserver(OnViewModelChangObserver observer) {
		m_changeObserver = observer;
	}
	
	public void UnregisterViewModelChangeObserver() {
		m_changeObserver = null;
	}

}
