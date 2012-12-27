package com.athena.asm.tool.infosense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 考虑如下电话号码的混淆方式：
 * 使用中文：〇一二三四五六七八九,零幺洞拐, 来代替相应数字
 * 1. 括号不会被嵌套
 * 2. 段与段之间只有不超过1个WhiteSpace
 * 3. '-'不能作为开头
 * 4. 电话号码必须>=5位，纯中文书写的则必须>=8位
 * @author aleck
 *
 */
public class PhoneNumSensor extends Sensor {
	private static final Pattern phone = Pattern.compile(
			"((\\+?)(([0-9PpWw〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐])+|\\(([0-9PpWw〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐])+\\)))" +
			"((\\s|[\\+\\-])?(([0-9PpWw〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐])+|\\(([0-9PpWw〇一二三四五六七八九零壹贰叁肆伍陆柒捌玖洞幺参拐])+\\)))*"
			);
	private static final Map<Character, Character> chinese;

	static {
		chinese = new HashMap<Character, Character>();
		// simple form
		chinese.put('〇', '0');
		chinese.put('一', '1');
		chinese.put('二', '2');
		chinese.put('三', '3');
		chinese.put('四', '4');
		chinese.put('五', '5');
		chinese.put('六', '6');
		chinese.put('七', '7');
		chinese.put('八', '8');
		chinese.put('九', '9');
		// capital form
		chinese.put('零', '0');
		chinese.put('贰', '1');
		chinese.put('叁', '2');
		chinese.put('肆', '3');
		chinese.put('伍', '4');
		chinese.put('陆', '5');
		chinese.put('柒', '6');
		chinese.put('捌', '7');
		chinese.put('玖', '8');
		chinese.put('拾', '9');
		// others
		chinese.put('洞', '0');
		chinese.put('幺', '1');
		chinese.put('参', '3');
		chinese.put('拐', '7');
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
			} else if (chinese.containsKey(ch)) {
				// translate chinese
				output.append(chinese.get(ch));
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

	private boolean satisfyLengthConstraint(String content) {
		return (content.length() >= 8 || !isPureChinese(content) && content.length() >= 5);
	}

	private boolean isPureChinese(String content) {
		for (int i = 0; i < content.length(); i++) {
			Character ch = content.charAt(i);
			if (!chinese.containsKey(ch)) {
				return false;
			}
		}
		return true;
	}

}
