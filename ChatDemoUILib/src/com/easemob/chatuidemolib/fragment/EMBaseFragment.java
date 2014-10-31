package com.easemob.chatuidemolib.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class EMBaseFragment extends Fragment{
	

	/**
	 * 生成顶部标题栏以下的主体layout(layout里不需再加标题栏)
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
	
}
