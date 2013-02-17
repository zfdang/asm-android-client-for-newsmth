package com.athena.asm.tool.notifier.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.markup.Markup;
import com.athena.asm.tool.notifier.markup.MarkupUtils;

public class Selector {
	private final boolean requirePostContent;
	private List<Criteria> criterias;

	/**
	 * 构造器
	 * @author aleck
	 *
	 */
	public static class SelectorBuilder {
		private List<Criteria> criterias = new ArrayList<Criteria>();
		
		public static SelectorBuilder create() {
			return new SelectorBuilder();
		}
		
		public SelectorBuilder addCriteria(Criteria pc) {
			criterias.add(pc);
			return this;
		}
		
		public SelectorBuilder addKeywordCriteria(PostField field, KeywordCriteria.Type type, String keyword, 
				boolean caseSensitive, boolean wholeWordOnly) {
			criterias.add(new KeywordCriteria(field, type, keyword, caseSensitive, wholeWordOnly));
			return this;
		}
		
		public SelectorBuilder addDateCriteria(DateCriteria.Type type, Date base) {
			criterias.add(new DateCriteria(type, base));
			return this;
		}
		
		public SelectorBuilder addAuthorCriteria(String expected, boolean caseSensitive) {
			criterias.add(new AuthorCriteria(expected, caseSensitive));
			return this;
		}
		
		public SelectorBuilder addAttachmentCriteria() {
			criterias.add(new AttachmentCriteria());
			return this;
		}
		
		public Selector build() {
			return new Selector(criterias);
		}
	}
	
	private Selector(List<Criteria> criterias) {
		this.criterias = new ArrayList<Criteria>(criterias);
		requirePostContent = calcRequirePostContent();
	}
	
	
	/**
	 * 是否需要Post.content进行判断
	 * @return
	 */
	public boolean requirePostContent() {
		return requirePostContent;
	}
	
	
	/**
	 * 一个对象是否符合达到条件
	 * @param entry
	 * @return
	 */
	public boolean qualify(Post post) {
		boolean failed = false;
		for (Criteria pc : criterias) {
			if (!pc.qualify(post)) {
				failed = true;
				break;
			}
		}
		return !(failed);
	}

	/**
	 * 将所有匹配的地方标记出来
	 * @param post
	 * @return
	 */
	public Map<PostField, List<Markup>> mark(Post post) {
		List<Markup> markups = new ArrayList<Markup>();
		for (Criteria pc : criterias) {
			markups.addAll(pc.mark(post));
		}
		return MarkupUtils.tidy(markups);
	}
	
	/**
	 * 将collection中的所有达到要求的元素选取出来
	 * @param collection
	 * @return
	 */
	public List<Post> filter(Collection<Post> collection) {
		List<Post> ret = new ArrayList<Post>();
		for (Post entry : collection) {
			if (qualify(entry)) {
				ret.add(entry);
			}
		}
		return ret;
	}
	
	/**
	 * 计算是否依赖于Post.content
	 * @return
	 */
	private boolean calcRequirePostContent() {
		boolean requires = false;
		for (Criteria pc : criterias) {
			if (pc.requirePostContent()) {
				requires = true;
				break;
			}
		}
		return requires;
	}
}
