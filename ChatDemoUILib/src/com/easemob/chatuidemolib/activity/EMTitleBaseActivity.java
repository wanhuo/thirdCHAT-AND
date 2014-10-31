package com.easemob.chatuidemolib.activity;

import com.easemob.chatuidemolib.R;
import com.easemob.chatuidemolib.view.EMTilteHeaderBar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class EMTitleBaseActivity extends BaseActivity{
	/**
	 * 顶部标题栏
	 */
	protected EMTilteHeaderBar mTitleHeaderBar;
	/**
	 * 布局主题内容Container
	 */
	protected LinearLayout mContentContainer;
	
	protected LayoutInflater mLayoutInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_title_base);
		mLayoutInflater = LayoutInflater.from(this);
		
		LinearLayout contentContainer = (LinearLayout) findViewById(R.id.ly_main_content_container);
		 // 顶部标题栏
        mTitleHeaderBar = (EMTilteHeaderBar) findViewById(R.id.title_header_bar);
        if (enableDefaultBack()) {
            mTitleHeaderBar.getLeftTextView().setBackgroundResource(R.drawable.mm_title_back);
            mTitleHeaderBar.setLeftOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	//返回事件
                	onActivityBackPressed();
                }
            });
        } else {
            mTitleHeaderBar.getLeftViewContainer().setVisibility(View.INVISIBLE);
        }

        mContentContainer = contentContainer;
        View contentView = mLayoutInflater.inflate(setContentLayoutId(),null);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        //加入主体布局
        contentContainer.addView(contentView);
        initView();
        setUpView();
	}
	
	
	/**
     * 是否使用默认的返回处理(默认true)
     *
     * @return
     */
    protected boolean enableDefaultBack() {
        return true;
    }
    
    protected boolean enableDefaultTitleBar() {
    	return true;
    }
	
    /**
     * 设置标题
     *
     * @param id
     */
    protected void setHeaderTitle(int id) {
        mTitleHeaderBar.getTitleTextView().setText(id);
    }

    /**
     * 设置标题
     * @param title
     */
    protected void setHeaderTitle(String title) {
        mTitleHeaderBar.getTitleTextView().setText(title);
    }
    
    /**
     * 获取标题栏右上角里的textview
     * @return
     */
    protected TextView getHeaderRightTextView(){
		return mTitleHeaderBar.getRightTextView();
    }

    /**
     * 获取顶部标题layout
     * @return
     */
    public EMTilteHeaderBar getTitleHeaderBar() {
        return mTitleHeaderBar;
    }
    
	private void onActivityBackPressed() {
        onBackPressed();
    }
	
	/**
	 * 设置布局id
	 */
	protected abstract int setContentLayoutId();
	
	protected abstract void setUpView();

	protected abstract void initView();
}
