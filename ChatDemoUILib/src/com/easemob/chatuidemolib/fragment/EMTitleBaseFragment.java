/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.chatuidemolib.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chatuidemolib.R;
import com.easemob.chatuidemolib.view.EMTilteHeaderBar;

/**
 * 带标题栏的fragment
 */
public abstract class EMTitleBaseFragment extends EMBaseFragment{

	/**
	 * 顶部标题栏
	 */
	protected EMTilteHeaderBar mTitleHeaderBar;
	/**
	 * 布局主题内容Container
	 */
    protected LinearLayout mContentContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.em_fragment_title_base, null);
        LinearLayout contentContainer = (LinearLayout) view.findViewById(R.id.ly_main_content_container);

        // 顶部标题栏
        mTitleHeaderBar = (EMTilteHeaderBar) view.findViewById(R.id.title_header_bar);
        if (enableDefaultBack()) {
            mTitleHeaderBar.getLeftTextView().setBackgroundResource(R.drawable.mm_title_back);
            mTitleHeaderBar.setLeftOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	//返回事件
                    onBackPressed();
                }
            });
        } else {
            mTitleHeaderBar.getLeftViewContainer().setVisibility(View.INVISIBLE);
        }

        mContentContainer = contentContainer;
        View contentView = createView(inflater, container, savedInstanceState);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        //加入主体布局
        contentContainer.addView(contentView);
        return view;
    }
    

    private void onBackPressed() {
        getActivity().onBackPressed();
    }

    /**
     * 是否使用默认的返回处理(默认true)
     *
     * @return
     */
    protected boolean enableDefaultBack() {
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
     * 获取顶部标题layout
     * @return
     */
    public EMTilteHeaderBar getTitleHeaderBar() {
        return mTitleHeaderBar;
    }
    
    /**
     * 获取标题栏右上角里的textview
     * @return
     */
    protected TextView getHeaderBarRightTextView(){
		return mTitleHeaderBar.getRightTextView();
    }
	
}
