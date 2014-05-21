package com.easemob.chatuidemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
/**
 * 自定义可以监听软键盘弹出和隐藏的View
 * @author liyuzhao
 *
 */
public class ResizeRelativeLayout extends RelativeLayout{
	 
	public ResizeRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	private OnResizeListener mListener;
	public interface OnResizeListener{
		void OnResize(int w,int h,int oldw,int oldh);
	}
	public void setOnResizeListener(OnResizeListener l)
	{
		mListener=l;
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(mListener!=null)
		{
			mListener.OnResize(w, h, oldw, oldh);
		}
	}
	 
	
	
	

}
