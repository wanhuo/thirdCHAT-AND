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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.easemob.chatuidemolib.R;
import com.easemob.chatuidemolib.adapter.EmojiAdapter;
import com.easemob.chatuidemolib.adapter.ExpressionPagerAdapter;
import com.easemob.chatuidemolib.view.ExpandGridView;
import com.easemob.chatuidemolib.view.emojicon.CirclePageIndicator;
import com.easemob.chatuidemolib.view.emojicon.Emojicon;
import com.easemob.chatuidemolib.view.emojicon.People;

public class EmojiconFragment extends Fragment {
	private OnEmojiconClickedListener mOnEmojiconClickedListener;
	private List<Emojicon> emojiDatas;
	private PagerAdapter emojisAdapter;
	private ViewPager emojisViewpager;
	// 排除删除键
	private final int iconSize = 24 - 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_emojicon, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		emojiDatas = Arrays.asList(People.DATA);

		emojisViewpager = (ViewPager) getView().findViewById(R.id.vPager);
		emojisAdapter = new ExpressionPagerAdapter(getEmojiGridViews());
		// 设置viewpager adapter
		emojisViewpager.setAdapter(emojisAdapter);
		CirclePageIndicator indicator = (CirclePageIndicator) getView().findViewById(R.id.indicator);
		indicator.setViewPager(emojisViewpager);
		indicator.setSnap(true);
	}

	/**
	 * 获取emoji gridviews
	 * 
	 * @return
	 */
	List<View> getEmojiGridViews() {
		List<View> views = new ArrayList<View>();
		int pageSize = emojiDatas.size() / iconSize;
		if (emojiDatas.size() % iconSize != 0)
			pageSize++;
		for (int i = 0; i < pageSize; i++) {
			View view = View.inflate(getActivity(), R.layout.expression_gridview, null);
			ExpandGridView gridView = (ExpandGridView) view.findViewById(R.id.gridview);
			List<Emojicon> subIcons = new ArrayList<Emojicon>();
			for (int j = i * iconSize; j < (i + 1) * iconSize; j++) {
				Emojicon emojicon;
				try {
					emojicon = emojiDatas.get(j);
					subIcons.add(emojicon);
				} catch (IndexOutOfBoundsException e) {
					break;
				}

			}
			subIcons.add(new Emojicon(""));
			// 设置gridview adapter
			gridView.setAdapter(new EmojiAdapter(getActivity(), subIcons));
			// 设置gridview item点击事件
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (position == parent.getCount() - 1)
						mOnEmojiconClickedListener.onEmojiconDeleteClicked();
					else {
						Emojicon emojicon = (Emojicon) parent.getItemAtPosition(position);
						// 通知item被点击
						mOnEmojiconClickedListener.onEmojiconClicked(emojicon);
					}
				}
			});
			views.add(view);
		}
		return views;
	}

	/**
	 * 输入emoji表情
	 * 
	 * @param editText
	 * @param emojicon
	 */
	public void inputEmojicon(EditText editText, Emojicon emojicon) {
		if (editText == null || emojicon == null) {
			return;
		}

		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		if (start < 0) {
			editText.append(emojicon.getEmoji());
		} else {
			editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
		}
	}
	
	/**
	 * 删除表情
	 * @param editText
	 */
	public void deleteEmojicon(EditText editText){
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// 设置OnEmojiconClickedListener
		if (activity instanceof OnEmojiconClickedListener) {
			mOnEmojiconClickedListener = (OnEmojiconClickedListener) activity;
		} else if (getParentFragment() instanceof OnEmojiconClickedListener) {
			mOnEmojiconClickedListener = (OnEmojiconClickedListener) getParentFragment();
		} else {
			throw new IllegalArgumentException(activity + " must implement interface " + OnEmojiconClickedListener.class.getSimpleName());
		}
	}

	@Override
	public void onDetach() {
		mOnEmojiconClickedListener = null;
		super.onDetach();
	}

	public interface OnEmojiconClickedListener {
		/**
		 * 表情被点击
		 * 
		 * @param emojicon
		 */
		void onEmojiconClicked(Emojicon emojicon);

		/**
		 * 删除表情
		 */
		void onEmojiconDeleteClicked();
	}
}
