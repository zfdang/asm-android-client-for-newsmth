package com.athena.asm.tool.infosense;

import java.util.List;

import junit.framework.TestCase;


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
 * 2. '@' 可以使用 "#", "_at_", "(at)"代替
 * 3. '.' 可以使用" dot ", "_dot_", "(dot)", "点"代替
 * 
 */
public class EmailAddrSensorTest extends TestCase {
	private void printInfos(List<Info> infos) {
		System.out.println("size=" + infos.size());
		for (Info info : infos) {
			System.out.println("original=" + info.original);
			System.out.println("content=" + info.content);
		}
	}

	private boolean contains(List<Info> infos, String[] hit) {
		for (String h : hit) {
			boolean found = false;
			for (Info info : infos) {
				if (info.content.equals(h)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}
	
	public void test01() {
		final String text = "Hi, this is abc@example.com. " +
				"I use my email a#b(dot)c. " + 
				"如果找不到我，也可以使用 xx (dot) yy (at) tsinghua 点 edu 点 cn";
		final String[] hit = new String[] {
			"abc@example.com",
			"a@b.c",
			"xx.yy@tsinghua.edu.cn"
		};
		EmailAddrSensor sensor = new EmailAddrSensor();
		List<Info> infos = sensor.scan(text);
		assertNotNull(infos);
		printInfos(infos);
		assertTrue("email addr not discovered", 
				contains(infos, hit));
		assertEquals("#found != #expected", 
				infos.size(), hit.length);
	}
}
