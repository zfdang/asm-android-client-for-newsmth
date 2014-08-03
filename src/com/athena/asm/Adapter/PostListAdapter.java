package com.athena.asm.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Attachment;
import com.athena.asm.data.Post;
import com.athena.asm.fragment.PostListFragment;
import com.athena.asm.util.StringUtility;
import com.athena.asm.view.GifWebView;
import com.athena.asm.view.LinkTextView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class PostListAdapter extends BaseAdapter implements OnClickListener, SectionIndexer{

	private PostListFragment m_fragment;
	private LayoutInflater m_inflater;
	private List<Post> m_postList;

    private String[] sections;
	
	// http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	// use ViewHolder pattern
	public class ViewHolder {
		public TextView authorTextView;
        public TextView indexTextView;
		public TextView titleTextView;
		public View lineView;
		public LinkTextView contentTextView;
		public TextView attachTextView;
		public LinearLayout imageLayout;
		public TextView dateTextView;
		public Post post;
	}

	public PostListAdapter(PostListFragment fragment, LayoutInflater inflater,
			List<Post> postList) {
		this.m_fragment = fragment;
		this.m_inflater = inflater;
		this.m_postList = postList;

		updateIndexer();

		// set parameters for UrlImageViewHelper
		// set threshold for max image size
		UrlImageViewHelper.setMaxImageSize(getMaxImageSize());

		UrlImageViewHelper.setUseZoomOut(true); // always enable zoom out

		// set error resource, this resource will be used when failed to load image
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			UrlImageViewHelper.setErrorResource(R.drawable.failure_night);
		} else {
			UrlImageViewHelper.setErrorResource(R.drawable.failure_day);
		}
	}

	public void updateIndexer()
	{
	    // prepare indexer for posts
        ArrayList<String> _sections = new ArrayList<String>();
        for (int i = 0; i < m_postList.size(); i++)
        {
            String index = m_postList.get(i).getPostIndex();
            if(index != null)
            {
                if(index.endsWith("楼"))
                    index = index.substring(0, index.length()-1);
                _sections.add(index);
            }
        }
        sections = _sections.toArray(new String[_sections.size()]);

        // force system to call getSections() again
        m_fragment.getListView().setFastScrollEnabled(false);
        m_fragment.getListView().setFastScrollEnabled(true);
        m_fragment.getListView().setFastScrollAlwaysVisible(true);
	}

	// image size > threshold won't be loaded in 2G/3G
	private int getMaxImageSize() {
		boolean isAutoOptimize = aSMApplication.getCurrentApplication().isAutoOptimize();
		// 非自动优化
		if( !isAutoOptimize )
			return 0;

		Context context = aSMApplication.getCurrentApplication().getApplicationContext();
		ConnectivityManager connectionManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
		if (networkInfo == null)
			// no active network connection
			return 0;
		int netType = networkInfo.getType();
		// WIFI下全部下载
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return 0;
		}

		// 自动优化且在移动网络中，返回阈值
		float threshold = aSMApplication.getCurrentApplication().getImageSizeThreshold();
		return (int)threshold * 1024;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		RelativeLayout layout = null;
		Post post = m_postList.get(position);
		
		if (convertView == null) {
			layout = (RelativeLayout) m_inflater.inflate(R.layout.post_list_item, null);
			holder = new ViewHolder();
			holder.authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
			holder.indexTextView = (TextView) layout.findViewById(R.id.PostIndex);
			holder.titleTextView = (TextView) layout.findViewById(R.id.PostTitle);
			holder.lineView = (View) layout.findViewById(R.id.SeperatorView);
			holder.contentTextView = (LinkTextView) layout.findViewById(R.id.PostContent);
			holder.attachTextView = (TextView) layout.findViewById(R.id.PostAttach);
			holder.imageLayout = (LinearLayout) layout.findViewById(R.id.imageLayout);
			holder.dateTextView = (TextView) layout.findViewById(R.id.PostDate);
			holder.post = post;
			
			int fontSize = aSMApplication.getCurrentApplication().getPostFontSize();
            holder.indexTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize - 2);
			holder.authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize - 2);
			holder.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize - 2);
			holder.dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize - 4);
			holder.contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
			holder.attachTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize - 2);
			
			layout.setTag(holder);
		}
		else {
			layout = (RelativeLayout)convertView;
			holder = (ViewHolder)layout.getTag();
			holder.post = post;
			layout.setTag(holder);
		}		
		
		if (post.getAuthor() == null) {
			holder.titleTextView.setText("错误的文章号,原文可能已经被删除");
			return layout;
		}
		
	    holder.authorTextView.setText(post.getAuthor());
		holder.indexTextView.setText(post.getPostIndex());
		holder.titleTextView.setText(post.getTitle());
		if (post.getTitle() == null) {
			holder.titleTextView.setHeight(0);
			holder.lineView.setVisibility(View.INVISIBLE);
			holder.attachTextView.setHeight(0);
		}						

		holder.contentTextView.setText(post.getContent());
		// Vt100TerminalModel.handleContent(post.getContent(), holder.contentTextView);
        Linkify.addLinks(holder.contentTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

		holder.attachTextView.setMovementMethod(LinkMovementMethod.getInstance());
		ArrayList<Attachment> attachments = post.getAttachFiles();
		if (attachments != null) {
			holder.imageLayout.removeAllViews();
			StringBuilder contentBuilder = new StringBuilder();
			contentBuilder.append("");

			if (attachments.size() >= 16){
				// 如果照片数量多余16张，不再放大图片
				UrlImageViewHelper.setUseZoomIn(false);
			}
			else{
				UrlImageViewHelper.setUseZoomIn(true);
			}

			// TODO: pass screen orientation to UrlImageViewHelper
			for (int i = 0; i < attachments.size(); i++) {
				Attachment attachment = attachments.get(i);
                String attachUrl = attachment.getAttachUrl();
				contentBuilder.append("<a href='").append(attachUrl).append("'>");
				contentBuilder.append(attachment.getName()).append("</a><br/>");
				String fileType = attachment.getName().toLowerCase();
				if(fileType.endsWith("gif")){
                    // Log.d("image", attachUrl);
				    GifWebView view = new GifWebView(m_fragment.getActivity(), attachUrl, attachment.getName());
                    LayoutParams layoutParams = new LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(1, 1, 1, 1);
                    view.setLayoutParams(layoutParams);
                    view.setTag(R.id.tag_first, i);
                    view.setTag(R.id.tag_second, attachments);
                    // http://stackoverflow.com/questions/3916330/android-webview-webpage-should-fit-the-device-screen
                    view.getSettings().setLoadWithOverviewMode(true);
                    view.getSettings().setUseWideViewPort(true);
                    holder.imageLayout.addView(view);
                    view.setOnClickListener(this);
				} else if (fileType.endsWith("jpg")
						|| fileType.endsWith("jpeg")
						|| fileType.endsWith("png") || fileType.endsWith("bmp")) {
					// Log.d("image", attachUrl);
					ImageView imageView = new ImageView(m_fragment.getActivity());
					LayoutParams layoutParams = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(1, 1, 1, 1);
					imageView.setLayoutParams(layoutParams);
					imageView.setTag(R.id.tag_first, i);
					imageView.setTag(R.id.tag_second, attachments);
					holder.imageLayout.addView(imageView);
					if (aSMApplication.getCurrentApplication().isNightTheme()) {
						UrlImageViewHelper.setUrlDrawable(imageView, attachUrl, R.drawable.loading_night);
					} else {
						UrlImageViewHelper.setUrlDrawable(imageView, attachUrl, R.drawable.loading_day);
					}
					imageView.setOnClickListener(this);
				}
			}
			holder.attachTextView.setText(Html.fromHtml(contentBuilder.toString()));
		}

		holder.dateTextView.setText(StringUtility.getFormattedString(post.getDate()));

		
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			holder.titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			holder.indexTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			holder.contentTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			holder.attachTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			holder.authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
		}

		// hide titles for all following posts
		if (position != 0) {
		    holder.titleTextView.setVisibility(View.GONE);
		} else {
            holder.titleTextView.setVisibility(View.VISIBLE);
		}
		return layout;
	}

	@Override
	public int getCount() {
		return m_postList.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= m_postList.size()) {
			position = m_postList.size() - 1;
		}
		return m_postList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClassName("com.athena.asm",
				"com.athena.asm.FullImageActivity");
		int attachmentIdx = (Integer) v.getTag(R.id.tag_first);
		@SuppressWarnings("unchecked")
		ArrayList<Attachment> attachments = (ArrayList<Attachment>) v.getTag(R.id.tag_second);
		ArrayList<String> urlList = new ArrayList<String>();
		ArrayList<String> fnameList = new ArrayList<String>();
		for (int i = 0; i < attachments.size(); i++) {
			urlList.add(attachments.get(i).getAttachUrl());
			fnameList.add(attachments.get(i).getName());
		}
		intent.putExtra(StringUtility.IMAGE_INDEX, attachmentIdx);
		intent.putStringArrayListExtra(StringUtility.IMAGE_URL, urlList);
		intent.putStringArrayListExtra(StringUtility.IMAGE_NAME, fnameList);
		m_fragment.startActivityForResult(intent, 0);
	}

    @Override
    public int getPositionForSection(int section) {
        // TODO Auto-generated method stub
        return section;
    }

    @Override
    public int getSectionForPosition(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public Object[] getSections() {
        // TODO Auto-generated method stub
        return sections;
    }
}
