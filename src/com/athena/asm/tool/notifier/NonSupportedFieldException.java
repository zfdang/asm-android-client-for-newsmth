package com.athena.asm.tool.notifier;

import com.athena.asm.tool.notifier.selector.Criteria;

@SuppressWarnings("serial")
public class NonSupportedFieldException extends IllegalArgumentException {
	public NonSupportedFieldException(Class<? extends Criteria> clazz, PostField field) {
		super(clazz.getName() + " is NOT applicable to field: " + field);
	}
}
