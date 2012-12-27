package com.athena.asm.tool.infosense;

import java.util.List;

/**
 * 从文本中发掘信息
 * @author aleck
 *
 */
public abstract class Sensor {
	public static enum Type {
		PHONE_NUMBER,
		EMAIL_ADDR,
		MULTIPLE,
	}
	
	protected final Type type;
	
	protected Sensor(Type type) {
		this.type = type;
	}
	
	public abstract List<Info> scan(CharSequence text);
}
