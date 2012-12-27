package com.athena.asm.tool.notifier.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.athena.asm.data.Post;
import com.athena.asm.tool.notifier.PostField;

public class MarkupUtils {
	/**
	 * 返回一个映射:
	 * 1. 这个映射将markups分门别类排好
	 * 2. 并且按照从前往后排序
	 * 3. 将重叠的markup（有公共部分，相邻的不算）合并
	 * @param markups
	 * @return
	 */
	public static Map<PostField, List<Markup>> tidy(List<Markup> markups) {
		Map<PostField, List<Markup>> ret = new HashMap<PostField, List<Markup>>();
		Post post = null;
		// collect
		for (Markup m : markups) {
			if (post != null && m.post != post) {
				throw new RuntimeException("Markups for multiple post on tidy().");
			} else {
				List<Markup> ms = null;
				if (ret.containsKey(m.field)) {
					ms = ret.get(m.field);
				} else {
					ms = new ArrayList<Markup>();
					ret.put(m.field, ms);
				}
				ms.add(m);
			}
		}
		// sort
		for (PostField field : ret.keySet()) {
			List<Markup> ms = ret.get(field);
			Collections.sort(ms, new Comparator<Markup>() {
				@Override
				public int compare(Markup m1, Markup m2) {
					return (m1.start - m2.start);
				}
			});
		}
		// merge
		for (PostField field : ret.keySet()) {
			List<Markup> ms = ret.get(field);
			merge(ms);
		}
		return ret;
	}

	/**
	 * 归并，前提条件:
	 * 1. 所有Markup来自同一个Post
	 * 2. 所有Markup来自同一个Field
	 * 3. 所有Markup已经排序好
	 * @param ms
	 */
	private static List<Markup> merge(List<Markup> markups) {
		List<Markup> ret = new ArrayList<Markup>(); 
		if (markups.size() <= 1) {
			return markups;
		} else {
			Markup head = markups.get(0);
			int i = 1;
			int start = head.start;
			int end = head.end;
			while (i < markups.size()) {
				Markup m = markups.get(i);
				if (end > m.start) {
					// 有公共部分
					if (m.end > end) {
						// 扩展
						end = m.end;
					} else {
						// 被包含
						// do nothing
					}
				} else {
					ret.add(new Markup(m.post, m.field, start, end));
					// 断开
					head = m;
					start = head.start;
					end = head.end;
				}
				i ++;
			}
			return ret;
		}
	}
}
