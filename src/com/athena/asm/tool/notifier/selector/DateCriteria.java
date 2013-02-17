package com.athena.asm.tool.notifier.selector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.markup.Markup;

public class DateCriteria extends Criteria {

	public static enum Type {
		LATER_THAN,
		EARLIER_THAN,
	}
	
	public final Type type;
	public final Date base;
	
	public DateCriteria(Type type, Date base) {
		super(PostField.DATE);
		if (type == null || base == null) {
			throw new IllegalArgumentException("type and base date should not be null.");
		}
		this.type = type;
		this.base = base;
	}
	
	@Override
	public boolean requirePostContent() {
		return false;
	}

	@Override
	public boolean qualify(Post post) {
		Date date = post.getDate();
		if (date == null) {
			return false;
		} else {
			if (type == Type.LATER_THAN) {
				return date.equals(base) || date.after(base);
			} else if (type == Type.EARLIER_THAN) {
				return date.equals(base) || date.before(base);
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean applicable(PostField field) {
		return (field == PostField.DATE);
	}

	@Override
	public List<Markup> mark(Post post) {
		return new ArrayList<Markup>();
	}

}
