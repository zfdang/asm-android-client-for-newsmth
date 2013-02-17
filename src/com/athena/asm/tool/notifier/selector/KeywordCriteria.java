package com.athena.asm.tool.notifier.selector;

import java.util.ArrayList;
import java.util.List;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.markup.Markup;

/**
 * 删选规则
 * @author aleck
 *
 * WholeWordOnly参数只对英文的Pattern有效。
 * 不用于正则表达式中的\bword\b，这里实际上只要求匹配时的前后分别来自于以下的1,2类，或者均来自于2类。
 * 在处理中文时有必要，比如 ABC单词，如果使用 \bABC\b去匹配，是找不到的。
 * 1. [a-zA-Z0-9]或者'-'
 * 2. 其余字母
 * 
 */
public class KeywordCriteria extends Criteria {
	public static enum Type {
		REQUIRE,
		AVOID,
	}
	
	private static final String WORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	
	public final Type type;
	public final String keyword;
	public final boolean caseSensitive;
	public final boolean wholeWordOnly;
	
	public KeywordCriteria(PostField field, Type type, String keyword, 
			boolean caseSensitive, boolean wholeWordOnly) {
		super(field);
		this.type = type;
		this.keyword = (caseSensitive ? keyword : keyword.toUpperCase());
		this.caseSensitive = caseSensitive;
		this.wholeWordOnly = wholeWordOnly;
	}

	@Override
	public boolean qualify(Post post) {
		String target;
		if (field == PostField.TITLE) {
			target = post.getTitle();
		} else if (field == PostField.CONTENT) {
			target = post.getContent().toString();
		} else {
			target = null;
		}
		// 如果是null则直接不匹配
		if (target == null) {
			return false;
		} else {
			if (!caseSensitive) {
				target = target.toUpperCase();
			}
			int idx, start = 0;
			boolean found = false;
			while ((idx = target.indexOf(keyword, start)) != -1) {
				found = !wholeWordOnly ||
						checkBoundary(target, idx - 1) && 
						checkBoundary(target, idx + keyword.length());
				if (found)
					break;
				start = idx + keyword.length();
			}
			return (type == Type.REQUIRE && found || type == Type.AVOID && !found);
		}
	}

	/**
	 * 检查 idx 和 idx+1 这两个字符是否构成一个 Boundary
	 * @param text
	 * @param idx
	 * @return
	 */
	private boolean checkBoundary(CharSequence text, int idx) {
		if (idx < 0 || idx + 1 >= text.length()) {
			// 两头
			return true;
		} else {
			char c1 = text.charAt(idx);
			char c2 = text.charAt(idx + 1);
			boolean wc1 = WORD_CHARS.indexOf(c1) != -1;
			boolean wc2 = WORD_CHARS.indexOf(c2) != -1;
			return (wc1 != wc2 || !wc1 && !wc2);
		}
	}

	@Override
	public boolean applicable(PostField field) {
		return (field == PostField.TITLE || field == PostField.CONTENT);
	}

	@Override
	public boolean requirePostContent() {
		return field == PostField.CONTENT;
	}

	@Override
	public List<Markup> mark(Post post) {
		if (type == Type.REQUIRE) {
			String target;
			if (field == PostField.TITLE) {
				target = post.getTitle();
			} else if (field == PostField.CONTENT) {
				target = post.getContent().toString();
			} else {
				target = null;
			}
			// 如果是null则直接不匹配
			if (target == null) {
				return new ArrayList<Markup>();
			} else {
				if (!caseSensitive) {
					target = target.toUpperCase();
				}
				List<Markup> markups = new ArrayList<Markup>();
				int idx, start = 0;
				while ((idx = target.indexOf(keyword, start)) != -1) {
					boolean found = !wholeWordOnly ||
							checkBoundary(target, idx - 1) && 
							checkBoundary(target, idx + keyword.length());
					if (found) {
						markups.add(new Markup(post, field, idx, idx + keyword.length()));
					}
					start = idx + keyword.length();
				}
				return markups;
			}
		} else {
			return new ArrayList<Markup>();
		}
	}
	
}
