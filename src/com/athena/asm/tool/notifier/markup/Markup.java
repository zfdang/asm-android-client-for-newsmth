package com.athena.asm.tool.notifier.markup;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;

/**
 * Matches post.field[start, end)
 * @author aleck
 *
 */
public class Markup {
	public final Post post;
	public final PostField field;
	public final int start;
	public final int end;
	
	public Markup(Post post, PostField field, int start, int end) {
		super();
		this.post = post;
		this.field = field;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return "mark: " + field + "[" + start + ", " + end + ")";
	}
	
}
