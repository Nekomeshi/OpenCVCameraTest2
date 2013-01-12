package com.nekomeshi312.opencvcameratest2;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CapturedImageView extends ImageView {

	private MatOfKeyPoint mKeypoints = null;
	private Mat mDescriptors = null;
	
	public CapturedImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public CapturedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public CapturedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the mKeypoints
	 */
	public MatOfKeyPoint getKeypoints() {
		return mKeypoints;
	}
	/**
	 * @param mKeypoints the mKeypoints to set
	 */
	public void setKeypoints(MatOfKeyPoint mKeypoints) {
		this.mKeypoints = mKeypoints;
	}

	/**
	 * @return the mDescriptors
	 */
	public Mat getDescriptors() {
		return mDescriptors;
	}
	/**
	 * @param mDescriptors the mDescriptors to set
	 */
	public void setDescriptors(Mat mDescriptors) {
		this.mDescriptors = mDescriptors;
	}
	
}
