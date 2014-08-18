package com.easemob.chatuidemo.activity;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.chatuidemo.R;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

public class RecorderVideoActivity extends BaseActivity implements
		OnClickListener, Callback, OnErrorListener, OnInfoListener {

	private ImageView btnStart;// 开始录制按钮
	private ImageView btnStop;// 停止录制按钮
	private MediaRecorder mediarecorder;// 录制视频的类
	private SurfaceView surfaceview;// 显示视频的控件

	private SurfaceHolder surfaceHolder;
	String localPath = "";// 录制的视频路径
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		// 选择支持半透明模式，在有surfaceview的activity中使用
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.recorder_activity);
		btnStart = (ImageView) findViewById(R.id.recorder_start);
		btnStop = (ImageView) findViewById(R.id.recorder_stop);
		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		SurfaceHolder holder = surfaceview.getHolder();// 取得holder
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void back(View view) {

		if (mediarecorder != null) {
			// 停止录制
			mediarecorder.stop();
			// 释放资源
			mediarecorder.release();
			mediarecorder = null;
		}
		try {
			mCamera.reconnect();
		} catch (IOException e) {
			Toast.makeText(this, "reconect fail", 0).show();
		}
		finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.recorder_start:
			mCamera.unlock();
			mediarecorder = new MediaRecorder();// 创建mediarecorder对象
			mediarecorder.reset();
			mediarecorder.setCamera(mCamera);
			mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			// 设置录制视频源为Camera（相机）
			mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
			mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			// 设置录制的视频编码h263 h264
			mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
			mediarecorder.setVideoSize(640, 480);
			// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
			mediarecorder.setVideoFrameRate(10);
			mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
			// 设置视频文件输出的路径
			localPath = PathUtil.getInstance().getVideoPath() + "/"
					+ System.currentTimeMillis() + ".mp4";
			mediarecorder.setOutputFile(localPath);
			mediarecorder.setOnErrorListener(this);
			mediarecorder.setOnInfoListener(this);
			try {
				// 准备录制
				mediarecorder.prepare();
				// 开始录制
				mediarecorder.start();
				Toast.makeText(this, "录像开始", Toast.LENGTH_SHORT).show();
				btnStart.setVisibility(View.INVISIBLE);
				btnStop.setVisibility(View.VISIBLE);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		case R.id.recorder_stop:

			if (mediarecorder != null) {
				// 停止录制
				mediarecorder.stop();
				// 释放资源
				mediarecorder.release();
				mediarecorder = null;
			}
			try {
				mCamera.reconnect();
			} catch (IOException e) {
				Toast.makeText(this, "reconect fail", 0).show();
			}
			btnStart.setVisibility(View.VISIBLE);
			btnStop.setVisibility(View.INVISIBLE);

			new AlertDialog.Builder(this)
					.setMessage("是否发送？")
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									arg0.dismiss();
									sendVideo(null);

								}
							}).setNegativeButton(R.string.cancel, null).show();

			break;

		default:
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		surfaceHolder = holder;
		initpreview();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// surfaceDestroyed的时候同时对象设置为null
		surfaceview = null;
		surfaceHolder = null;
		mediarecorder = null;
		releaseCamera();
	}

	protected void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@SuppressLint("NewApi")
	protected void initpreview() {
		try {

			mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
			mCamera.setPreviewDisplay(surfaceHolder);
			setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK,
					mCamera);
			mCamera.startPreview();
		} catch (Exception e) {
			EMLog.e("###", e.getMessage());
			showFailDialog();
			return;
		}

	}

	@SuppressLint("NewApi")
	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	MediaScannerConnection msc = null;

	public void sendVideo(View view) {
		if (TextUtils.isEmpty(localPath)) {
			EMLog.e("Recorder", "recorder fail please try again!");
			return;
		}

		msc = new MediaScannerConnection(this,
				new MediaScannerConnectionClient() {

					@Override
					public void onScanCompleted(String path, Uri uri) {
						System.out.println("scanner completed");
						msc.disconnect();
						setResult(RESULT_OK, getIntent().putExtra("uri", uri));
						finish();
					}

					@Override
					public void onMediaScannerConnected() {
						msc.scanFile(localPath, "video/*");
					}
				});
		msc.connect();

	}

	@Override
	public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(MediaRecorder arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();
	}

	@Override
	public void onBackPressed() {
		back(null);
	}
	
	
	
	
	
	private void showFailDialog(){
		new AlertDialog.Builder(this).setTitle("提示").setMessage("打开设备失败！").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				
			}
		}).setCancelable(false).show();
		
		
	}
	
	 
	

}
