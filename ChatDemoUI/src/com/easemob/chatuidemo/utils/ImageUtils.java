package com.easemob.chatuidemo.utils;

import com.easemob.util.EMLog;

public class ImageUtils {
	public static String getThumbnailImagePath(String imagePath) {
		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
		EMLog.d("msg", "original image path:" + imagePath);
		EMLog.d("msg", "thum image path:" + path);
		return path;
	}
}
