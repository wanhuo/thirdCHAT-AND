package com.easemob.chatuidemo.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.easemob.chat.EMChatConfig;
import com.easemob.chatuidemo.R;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.util.PathUtil;

/**
 * 展示视频内容
 * @author Administrator
 *
 */
public class ShowVideoActivity extends BaseActivity implements OnTouchListener{

	MediaController mController;
	VideoView videoView;
	int progress=0;
	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;
	private String localFilePath;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.showvideo_activity);
		videoView=(VideoView) findViewById(R.id.videoView);
		loadingLayout=(RelativeLayout) findViewById(R.id.loading_layout);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		mController=new MediaController(this);
		videoView.setMediaController(mController);
		Uri uri = getIntent().getParcelableExtra("uri");
		String remotepath=getIntent().getStringExtra("remotepath");
		String secret=getIntent().getStringExtra("secret");
		System.err.println("show video view uri:"+uri+" remotepath:"+remotepath+" secret:"+secret);
		if(uri!=null&&new File(uri.getPath()).exists())
		{
			videoView.setVideoURI(uri);
			videoView.requestFocus();
			videoView.start();
		}else if(!TextUtils.isEmpty(remotepath)&&!remotepath.equals("null")){
			 System.err.println("download remote video file");
			 Map<String,String> maps=new HashMap<String,String>();
			 maps.put("Authorization", "Bearer "+EMChatConfig.getInstance().AccessToken);
			 if(!TextUtils.isEmpty(secret))
			 {
				 maps.put("share-secret", secret);
			 }
			 maps.put("Accept", "application/octet-stream");
			 downloadVideo(remotepath,maps);
		}else{
			
		}
		
		 
		
	}
	
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		try {
			progress=videoView.getCurrentPosition();
		} catch (Exception e) {
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		try {
			videoView.seekTo(progress);
			videoView.start();
		} catch (Exception e) {
		}
	}
	
	 
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * 下载视频文件
	 */
	private void downloadVideo(final String remoteUrl,final Map<String,String> header)
	{
		 
		localFilePath = PathUtil.getInstance().getVideoPath().getAbsolutePath() + "/"
				+ remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
		
		if(new File(localFilePath).exists())
		{
			videoView.setVideoPath(localFilePath);
			videoView.requestFocus();
			videoView.start();
			return;
		}
		
		System.err.println("download view file ...");
		loadingLayout.setVisibility(View.VISIBLE);
		
		final HttpFileManager httpFileMgr=new HttpFileManager(this, EMChatConfig.getInstance().getStorageUrl());
		final CloudOperationCallback callback=new CloudOperationCallback() {
			
			@Override
			public void onSuccess(String result) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						loadingLayout.setVisibility(View.GONE);
						progressBar.setProgress(0);
						videoView.setVideoURI(Uri.fromFile(new File(localFilePath)));
						videoView.seekTo(0);
						videoView.requestFocus();
						videoView.start();
						
					}
				});
				
				
			}
			
			@Override
			public void onProgress(final int progress) {
				 Log.d("ease", "video progress:"+progress);
				 runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						progressBar.setProgress(progress);
						
					}
				});
				
			}
			
			@Override
			public void onError(String msg) {
				Log.e("###", "offline file transfer error:"+msg); 
				File file=new File(localFilePath);
				if(file.exists())
				{
					file.delete();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						 
						
					}
				});
				
			}
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				httpFileMgr.downloadFile(remoteUrl, localFilePath, EMChatConfig.getInstance().APPKEY,null,header,callback);
			}
		}).start();
		
		
		
		
	}
	
	
	
	
	
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
