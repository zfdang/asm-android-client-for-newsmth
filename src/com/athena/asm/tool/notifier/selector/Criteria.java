package com.athena.asm.tool.notifier.selector;

import java.util.List;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.NonSupportedFieldException;
import com.athena.asm.tool.notifier.PostField;
import com.athena.asm.tool.notifier.markup.Markup;

/**
 * 用于删选Post的原则
 * @author aleck
 *
 */
public abstract class Criteria {
	
	/**
	 * 是否需要Post的内容来进行判断
	 * @return
	 */
	public abstract boolean requirePostContent();
	
	/**
	 * 当前Post是否通过删选
	 * @param post
	 * @return
	 */
	public abstract boolean qualify(Post post);

	/**
	 * 当前Criteria是否可以适用与某个field
	 * @param field
	 * @return
	 */
	public abstract boolean applicable(PostField field);
	
	public abstract List<Markup> mark(Post post);
	
	protected final PostField field;
	
	public Criteria(PostField field) {
		if (!applicable(field)) {
			throw new NonSupportedFieldException(this.getClass(), field);
		}
		this.field = field;
	}
}
