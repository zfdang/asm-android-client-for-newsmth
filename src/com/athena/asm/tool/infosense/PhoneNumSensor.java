package com.athena.asm.tool.infosense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 考虑如下电话号码的混淆方式：
 * 使用中文：〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐oO①②③④⑤⑥⑦⑧⑨, 来代替相应数字
 * 1. 括号不会被嵌套
 * 2. 段与段之间只有不超过1个WhiteSpace
 * 3. '-'不能作为开头
 * 4. 电话号码必须>=5位，纯特殊字符书写的则必须>=8位
 * @author aleck
 *
 */
public class PhoneNumSensor extends Sensor {
	// 其余可能的字符，只用于检测，不要求specials映射对应（但最好是对应）
	private static final String OTHER_DIGITS = "〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐oO①②③④⑤⑥⑦⑧⑨";
	// 正则表达式
	private static final Pattern phone = Pattern.compile(
			"((\\+?)(([0-9PpWw" + OTHER_DIGITS + "])+|\\(([0-9PpWw" + OTHER_DIGITS + "])+\\)))" +
			"((\\s|[\\+\\-])?(([0-9PpWw" + OTHER_DIGITS + "])+|\\(([0-9PpWw" + OTHER_DIGITS + "])+\\)))*"
			);
	private static final Map<Character, Character> specials;

	static {
		specials = new HashMap<Character, Character>();
		// simple form
		specials.put('〇', '0');
		specials.put('一', '1');
		specials.put('二', '2');
		specials.put('三', '3');
		specials.put('四', '4');
		specials.put('五', '5');
		specials.put('六', '6');
		specials.put('七', '7');
		specials.put('八', '8');
		specials.put('九', '9');
		// capital form
		specials.put('零', '0');
		specials.put('贰', '1');
		specials.put('叁', '2');
		specials.put('肆', '3');
		specials.put('伍', '4');
		specials.put('陆', '5');
		specials.put('柒', '6');
		specials.put('捌', '7');
		specials.put('玖', '8');
		specials.put('拾', '9');
		// others
		specials.put('洞', '0');
		specials.put('幺', '1');
		specials.put('参', '3');
		specials.put('拐', '7');
		specials.put('o', '0');
		specials.put('O', '0');
		specials.put('①', '1');
		specials.put('②', '2');
		specials.put('③', '3');
		specials.put('④', '4');
		specials.put('⑤', '5');
		specials.put('⑥', '6');
		specials.put('⑦', '7');
		specials.put('⑧', '8');
		specials.put('⑨', '9');
	}

	protected PhoneNumSensor() {
		super(Type.PHONE_NUMBER);
	}
	
	public String normalize(String raw) {
		// 可以被去掉的字符集合
		// 数字，字母p和w不可以被去掉
		final String ignore = "+-()";
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			Character ch = raw.charAt(i);
			if (Character.isWhitespace(ch) || ignore.indexOf(ch) != -1) {
				// ignore
			} else if (specials.containsKey(ch)) {
				// translate
				output.append(specials.get(ch));
			} else {
				output.append(ch);
			}
		}
		return output.toString();
	}

	@Override
	public List<Info> scan(CharSequence text) {
		List<Info> ret = new ArrayList<Info>();
		Matcher matcher = phone.matcher(text);
		while (matcher.find()) {
			String original = matcher.group();
			String content = normalize(original);
			if (satisfyLengthConstraint(content)) {
				ret.add(new Info(Type.PHONE_NUMBER, content, original, matcher.start()));
			}
		}
		return ret;
	}

	/**
	 * 检查是否满足长度要求
	 * 1. 最少长度为8，对于纯中文串，最少长度为5
	 * 2. 最大长度为16
	 * @param content
	 * @return
	 */
	private boolean satisfyLengthConstraint(String content) {
		return (content.length() <= 16) && 
				(content.length() >= 8 || !isPureSpecial(content) && content.length() >= 5);
	}

	private boolean isPureSpecial(String content) {
		for (int i = 0; i < content.length(); i++) {
			Character ch = content.charAt(i);
			if (!specials.containsKey(ch)) {
				return false;
			}
		}
		return true;
	}

}
