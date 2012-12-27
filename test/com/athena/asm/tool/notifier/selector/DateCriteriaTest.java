package com.athena.asm.tool.notifier.selector;

import java.util.Date;

import junit.framework.TestCase;

import com.athena.asm.data.Post;

/**
 * 删选规则
 * @author aleck
 *
 */
public class DateCriteriaTest extends TestCase {
	public void test01() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.LATER_THAN, base);
		Post post = createPost(new Date(base.getTime()));
		assertTrue(kc.qualify(post));
	}
	
	public void test02() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.LATER_THAN, base);
		Post post = createPost(new Date(base.getTime() - 1));
		assertFalse(kc.qualify(post));
	}
	
	public void test03() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.LATER_THAN, base);
		Post post = createPost(new Date(base.getTime() + 1));
		assertTrue(kc.qualify(post));
	}
	
	public void test11() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.EARLIER_THAN, base);
		Post post = createPost(new Date(base.getTime()));
		assertTrue(kc.qualify(post));
	}
	
	public void test12() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.EARLIER_THAN, base);
		Post post = createPost(new Date(base.getTime() - 1));
		assertTrue(kc.qualify(post));
	}
	
	public void test13() {
		Date base = new Date();
		DateCriteria kc = new DateCriteria(DateCriteria.Type.EARLIER_THAN, base);
		Post post = createPost(new Date(base.getTime() + 1));
		assertFalse(kc.qualify(post));
	}
	
	private Post createPost(Date date) {
		Post post = new Post();
		post.setDate(date);
		return post;
	}
}
