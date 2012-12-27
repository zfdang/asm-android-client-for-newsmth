package com.athena.asm.tool.notifier.selector;

import java.util.Date;

import junit.framework.TestCase;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.selector.Selector.SelectorBuilder;

/**
 * 删选规则
 * @author aleck
 *
 */
public class SelectorTest extends TestCase {
	public void test01() {
		Date base = new Date();
		Post post = createPost(
				"转让到长沙的T1一张",
				"请联系我：幺三九-一二三四-二三肆一",
				"aleck",
				base
				);
		Selector selector = SelectorBuilder.create()
			.addDateCriteria(DateCriteria.Type.LATER_THAN, new Date(base.getTime() - 1))
			.addAuthorCriteria("aleck", false)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "T1", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.AVOID, "求购", true, true)
			.build();
		assertTrue(selector.qualify(post));
	}
	
	public void test02() {
		Date base = new Date();
		Post post = createPost(
				"求购到长沙的T1一张",
				"请联系我：幺三九-一二三四-二三肆一",
				"aleck",
				base
				);
		Selector selector = SelectorBuilder.create()
			.addDateCriteria(DateCriteria.Type.LATER_THAN, new Date(base.getTime() - 1))
			.addAuthorCriteria("aleck", false)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "T1", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.AVOID, "求购", true, true)
			.build();
		assertFalse(selector.qualify(post));
	}
	
	public void test03() {
		Date base = new Date();
		Post post = createPost(
				"转让到长沙的T1一张",
				"请联系我：幺三九-一二三四-二三肆一",
				"aleck",
				base
				);
		Selector selector = SelectorBuilder.create()
			.addDateCriteria(DateCriteria.Type.LATER_THAN, new Date(base.getTime() - 1))
			.addAuthorCriteria("aleck", false)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "T1", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "转让", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.AVOID, "求购", true, true)
			.build();
		assertTrue(selector.qualify(post));
	}
	
	/**
	 * 测试Markup
	 */
	public void test11() {
		Date base = new Date();
		Post post = createPost(
				"转让到长沙的T1一张，另外还有一张T145",
				"请联系我：幺三九-一二三四-二三肆一",
				"aleck",
				base
				);
		Selector selector = SelectorBuilder.create()
			.addDateCriteria(DateCriteria.Type.LATER_THAN, new Date(base.getTime() - 1))
			.addAuthorCriteria("aleck", false)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "T1", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.REQUIRE, "转让", true, true)
			.addKeywordCriteria(PostField.TITLE, KeywordCriteria.Type.AVOID, "求购", true, true)
			.build();
		assertTrue(selector.qualify(post));
		System.out.println(selector.mark(post));
	}
	
	private Post createPost(String title, String content, String author, Date date) {
		Post post = new Post();
		post.setTitle(title);
		post.setContent(content);
		post.setAuthor(author);
		post.setDate(date);
		return post;
	}
}
