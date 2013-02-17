package com.athena.asm.tool.notifier.selector;

import junit.framework.TestCase;

import com.athena.asm.data.Post;

/**
 * 删选规则
 * @author aleck
 *
 */
public class AuthorCriteriaTest extends TestCase {
	public void test01() {
		final String author = "ABC";
		final String lookfor = "abc";
		AuthorCriteria ac = new AuthorCriteria(lookfor, true);
		Post post = createPost(author);
		assertFalse(ac.qualify(post));
	}
	
	public void test02() {
		final String author = "ABC";
		final String lookfor = "abc";
		AuthorCriteria ac = new AuthorCriteria(lookfor, false);
		Post post = createPost(author);
		assertTrue(ac.qualify(post));
	}
	
	public void test03() {
		final String author = "ABC";
		final String lookfor = "ABC";
		AuthorCriteria ac = new AuthorCriteria(lookfor, false);
		Post post = createPost(author);
		assertTrue(ac.qualify(post));
	}
	
	public void test04() {
		final String author = "ABC";
		final String lookfor = "ABC";
		AuthorCriteria ac = new AuthorCriteria(lookfor, false);
		Post post = createPost(author);
		assertTrue(ac.qualify(post));
	}
	
	public void test05() {
		final String author = "XXXABCDEFG";
		final String lookfor = "ABC";
		AuthorCriteria ac = new AuthorCriteria(lookfor, false);
		Post post = createPost(author);
		assertFalse(ac.qualify(post));
	}
	
	private Post createPost(String author) {
		Post post = new Post();
		post.setAuthor(author);
		return post;
	}
}
