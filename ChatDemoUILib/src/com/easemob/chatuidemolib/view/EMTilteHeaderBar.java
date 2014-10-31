package com.easemob.chatuidemolib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.easemob.chatuidemolib.R;

/**
 * 普通标题栏
 * 
 */
public class EMTilteHeaderBar extends EMHeaderBase {
	private TextView mTitleTextView;
	private TextView mRightTextView;
	private TextView mLeftTextView;

	public EMTilteHeaderBar(Context context) {
		this(context, null);
	}

	public EMTilteHeaderBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EMTilteHeaderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mLeftTextView = (TextView) findViewById(R.id.tv_title_bar_left);
		mTitleTextView = (TextView) findViewById(R.id.tv_title_bar_title);
		mRightTextView = (TextView) findViewById(R.id.tv_title_bar_right);
	}

	/**
	 * 获取layout id
	 */
	@Override
	protected int getLayoutId() {
		return R.layout.em_layout_base_header_bar_title;
	}

	public TextView getLeftTextView() {
		return mLeftTextView;
	}

	public TextView getTitleTextView() {
		return mTitleTextView;
	}

	public TextView getRightTextView() {
		return mRightTextView;
	}

}
