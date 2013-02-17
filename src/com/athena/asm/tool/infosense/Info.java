package com.athena.asm.tool.infosense;

import com.athena.asm.tool.infosense.Sensor.Type;

/**
 * 记录一个从文本中发现的信息，目前可能是邮箱地址或者电话号码
 * @author aleck
 *
 */
public class Info {
	// 类型
	public final Type type;
	// 抽取之后的文本
	public final String content;
	// 原始的文本
	public final String original;
	// 原文中的起始位置
	public final int start;
	
	public Info(Type type, String content, String original, int start) {
		super();
		this.type = type;
		this.content = content;
		this.original = original;
		this.start = start;
	}
	
	@Override
	public String toString() {
		return "info:[" + type + "]:" + original + " --> " + content + " at " + start;
	}
}
