package com.athena.asm.tool.infosense;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从文本中解析出疑似的电子邮件
 * @author aleck
 * 
 * Email地址的详细定义可以参见：
 * http://en.wikipedia.org/wiki/E-mail_address#Valid_email_addresses
 * http://en.wikipedia.org/wiki/Hostname
 * 虽然Email地址本身约束很宽松，文中提到不同运营商会加入很多限制，而且各家不尽相同。
 * 所以这里还是尽量以自己的理解，处理生活中常见的情况
 * 1. LocalPart 可以出现[&'+-=_] + [a-z] + [A-Z] + [0-9]
 * 2. LocalPart 可以出现"."，前提是不是第一个或者最后一个字符
 * 3. DomainPart 是一个Hostname，可以出现的字符有 [a-z] + [A-Z] + [0-9] + [-] + [.]
 * 4. DomainPart 由'.'分割为若干段，每一段不能由'-'或者数字开头也不能由'-'结尾
 * 
 * 在此基础上，本函数识别如下不规范的写法
 * 1. '@'和'.'的前后可以有空格
 * 2. '@' 可以使用 "#", " at ", "_at_", "(at)"代替
 * 3. '.' 可以使用" dot ", "_dot_", "(dot)", "点"代替
 * 
 */
public class EmailAddrSensor extends Sensor {
	
	protected EmailAddrSensor() {
		super(Type.EMAIL_ADDR);
	}

	private static final Pattern email = Pattern.compile(
			// local part, group 1
			"(([&'+\\-=_a-zA-Z0-9]+(\\.|_dot_|\\s*(点|\\sdot\\s|\\(dot\\))\\s*))*[&'+\\-=_a-zA-Z0-9]+)" +
			// @, group 3
			"(\\s*(@|#|\\sat\\s|_at_|\\(at\\))\\s*)" +
			// domain part 4
			"(([a-zA-Z]+[a-zA-Z0-9\\-]*(\\.|_dot_|\\s*(点|\\sdot\\s|\\(dot\\))\\s*))*[a-zA-Z]+[a-zA-Z0-9\\-]*)"
			);

	private String normalize(String raw) {
		// this is a naive implementation, forgive me
		return raw.replaceAll("(#|_at_|\\(at\\))", "@")
				.replaceAll("(点|\\s+dot\\s+|_dot_|\\(dot\\))", ".")
				.replaceAll("\\s+", "");
	}
	
	@Override
	public List<Info> scan(CharSequence text) {
		List<Info> ret = new ArrayList<Info>();
		Matcher matcher = email.matcher(text);
		while (matcher.find()) {
			String original = matcher.group();
			String content = normalize(original);
			ret.add(new Info(Type.EMAIL_ADDR, content, original, matcher.start()));
		}
		return ret;
	}
	
}
