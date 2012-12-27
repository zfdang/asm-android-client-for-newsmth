package com.athena.asm.tool.notifier.selector;

import java.util.Collections;
import java.util.List;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.NonSupportedFieldException;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.markup.Markup;

/**
 * 对某一个Field进行严格比对
 * @author aleck
 *
 */
public class AuthorCriteria extends Criteria {

	public final String expected;
	private final String expectedUpperCase;
	public final boolean caseSensitive;
	
	public AuthorCriteria(String expected, boolean caseSensitive) {
		super(PostField.AUTHOR);
		if (expected == null) {
			throw new IllegalArgumentException("ExactCriteria does not accept 'null'");
		}
		this.expected = expected;
		this.expectedUpperCase = expected.toUpperCase();
		this.caseSensitive = caseSensitive;
	}
	
	@Override
	public boolean applicable(PostField field) {
		return (field == PostField.AUTHOR);
	}

	@Override
	public boolean qualify(Post post) {
		if (field == PostField.AUTHOR) {
			String author = post.getAuthor();
			if (author == null) {
				return false;
			} else {
				if (caseSensitive) {
					return expected.equals(author);
				} else {
					return expectedUpperCase.equals(author.toUpperCase());
				}
			}
		} else {
			throw new NonSupportedFieldException(this.getClass(), field);
		}
	}

	@Override
	public boolean requirePostContent() {
		return false;
	}

	@Override
	public List<Markup> mark(Post post) {
		return Collections.singletonList(
				new Markup(post, field, 0, expected.length()));
	}

}
