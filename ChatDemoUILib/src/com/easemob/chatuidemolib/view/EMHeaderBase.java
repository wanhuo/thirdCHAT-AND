package com.easemob.chatuidemolib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.easemob.chatuidemolib.R;

public class EMHeaderBase extends RelativeLayout {
	private RelativeLayout mLeftViewContainer;
	private RelativeLayout mRightViewContainer;
	private RelativeLayout mCenterViewContainer;

	public EMHeaderBase(Context context) {
		this(context, null);
	}

	public EMHeaderBase(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EMHeaderBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(getLayoutId(), this);
		mLeftViewContainer = (RelativeLayout) findViewById(R.id.rl_title_bar_left);
		mCenterViewContainer = (RelativeLayout) findViewById(R.id.rl_title_bar_center);
		mRightViewContainer = (RelativeLayout) findViewById(R.id.rl_title_bar_right);
	}

	protected int getLayoutId() {
		return R.layout.em_layout_base_header_bar_title;
	}

	public View getLeftViewContainer() {
		return mLeftViewContainer;
	}

	public View getCenterViewContainer() {
		return mCenterViewContainer;
	}

	public View getRigthViewContainer() {
		return mRightViewContainer;
	}

	public void setLeftOnClickListener(OnClickListener l) {
		mLeftViewContainer.setOnClickListener(l);
	}

	public void setCenterOnClickListener(OnClickListener l) {
		mCenterViewContainer.setOnClickListener(l);
	}

	public void setRightOnClickListener(OnClickListener l) {
		mRightViewContainer.setOnClickListener(l);
	}
}
