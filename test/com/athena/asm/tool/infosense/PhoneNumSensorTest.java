package com.athena.asm.tool.infosense;

import java.util.List;

import junit.framework.TestCase;


/**
 * 考虑如下电话号码的混淆方式：
 * 使用中文：〇一二三四五六七八九,零幺洞拐 来代替相应数字
 * 1. 括号不会被嵌套
 * 2. 段与段之间只有不超过1个WhiteSpace
 * @author aleck
 *
 */
public class PhoneNumSensorTest extends TestCase {
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
		final String text = "请联系我：一三九 六二七四 三五零一，或者：+86-151-2000-0001。" +
				"我的另外一个电话(6278)(1111)或者联系办公室+(86)-10-5153-0000p100或者拨打95588也可以找到我.";
		final String[] hit = new String[] {
				"13962743501",
				"8615120000001",
				"62781111",
				"861051530000p100",
				"95588"
		};
		PhoneNumSensor sensor = new PhoneNumSensor();
		List<Info> infos = sensor.scan(text);
		assertNotNull(infos);
		printInfos(infos);
		assertTrue("phone number not discovered", 
				contains(infos, hit));
		assertEquals("#found != #expected", 
				infos.size(), hit.length);
	}
}
