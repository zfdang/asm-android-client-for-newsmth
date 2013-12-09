package com.zfdang.asm.tool.notifier.selector;

import java.util.ArrayList;
import java.util.List;

import com.zfdang.asm.data.Post;
import com.zfdang.asm.tool.notifier.PostField;
import com.zfdang.asm.tool.notifier.markup.Markup;

public class AttachmentCriteria extends Criteria {

	public AttachmentCriteria() {
		super(PostField.ATTACHMENT);
	}
	
	@Override
	public boolean requirePostContent() {
		// TODO: to make sure
		return true;
	}

	@Override
	public boolean qualify(Post post) {
		return post.getAttachFiles() != null && post.getAttachFiles().size() > 0;
	}

	@Override
	public boolean applicable(PostField field) {
		return field == PostField.ATTACHMENT;
	}

	@Override
	public List<Markup> mark(Post post) {
		return new ArrayList<Markup>();
	}

}
