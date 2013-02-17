package com.athena.asm.tool.notifier.selector;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.selector.KeywordCriteria.Type;

import junit.framework.TestCase;

/**
 * 删选规则
 * @author aleck
 *
 */
public class KeywordCriteriaTest extends TestCase {
	public void test01() {
		Post post = createPost("转让T1一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.REQUIRE, "t1", false, true);
		assertTrue(kc.qualify(post));
	}

	public void test02() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.REQUIRE, "t1", false, true);
		assertFalse(kc.qualify(post));
	}

	public void test03() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.REQUIRE, "t1", false, false);
		assertTrue(kc.qualify(post));
	}

	public void test04() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.REQUIRE, "t1", true, false);
		assertFalse(kc.qualify(post));
	}

	public void test11() {
		Post post = createPost("转让T1一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.AVOID, "t1", false, true);
		assertFalse(kc.qualify(post));
	}

	public void test12() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.AVOID, "t1", false, true);
		assertTrue(kc.qualify(post));
	}

	public void test13() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.AVOID, "t1", false, false);
		assertFalse(kc.qualify(post));
	}

	public void test14() {
		Post post = createPost("转让T145一张", null);
		KeywordCriteria kc = new KeywordCriteria(PostField.TITLE, Type.AVOID, "t1", true, false);
		assertTrue(kc.qualify(post));
	}
	
	private Post createPost(String title, String content) {
		Post post = new Post();
		post.setTitle(title);
		post.setContent(content);
		return post;
	}
}
