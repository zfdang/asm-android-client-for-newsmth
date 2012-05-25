package com.athena.asm.viewmodel;

import java.util.List;

import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;

public class GuidanceListViewModel extends BaseViewModel {
	
	private List<String> m_guidanceSectionNames = null;
	private List<List<Subject>> m_guidanceSectionDetails = null;
	
	private SmthSupport m_smthSupport;
	
	public static final String GUIDANCE_PROPERTY_NAME = "Guidance";
	
	public GuidanceListViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public void clear() {
		m_guidanceSectionNames = null;
		m_guidanceSectionDetails = null;
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
	
	@SuppressWarnings("unchecked")
	public void updateGuidance() {
		Object[] guidance = m_smthSupport.getGuidance();
		setGuidanceSectionNames((List<String>) guidance[0]);
		setGuidanceSectionDetails((List<List<Subject>>) guidance[1]);
	}

	
	public void notifyGuidanceChanged() {
		notifyViewModelChange(this, GUIDANCE_PROPERTY_NAME);
	}
}
