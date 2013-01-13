package com.nekomeshi312.opencvcameratest2;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import com.nekomeshi312.opencvcameratest2.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;

public class MainActivity extends Activity
							implements CvCameraViewListener {
	private static final String LOG_TAG = "MainActivity";
	private CameraBridgeViewBase mOpenCvCameraView;
	private Mat mCapturedGray = null;
	private FeatureDetector mDetector;
	private DescriptorExtractor mDescriptor;
	private DescriptorMatcher mMatcher;
	private boolean mTouched = false;
	private CapturedImageView mImage[] = new CapturedImageView[3];
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mImage[0] = (CapturedImageView)findViewById(R.id.image_view1);
        mImage[1] = (CapturedImageView)findViewById(R.id.image_view2);
        mImage[2] = (CapturedImageView)findViewById(R.id.image_view3);
        mHandler = new Handler();

	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
       OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()){
			case MotionEvent.ACTION_UP:
				mTouched = true;
				break;
		}
		return true;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		mCapturedGray = new Mat(height, width, CvType.CV_8UC1);
		mDetector = FeatureDetector.create(FeatureDetector.PYRAMID_ORB);
		mDescriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		mMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);		
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		mCapturedGray.release();
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		// TODO Auto-generated method stub
		Imgproc.cvtColor(inputFrame, mCapturedGray, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		Mat descriptors = new Mat();
		//特徴点検出
		mDetector.detect(mCapturedGray, keypoints);
		mDescriptor.compute(mCapturedGray, keypoints, descriptors);
		//タッチがされた場合はImageViewにキャプチャした画像を追加
		final Mat rgb = inputFrame;
		if(mTouched){
			for(final CapturedImageView iv:mImage){
				if(iv.getKeypoints() == null){
					iv.setKeypoints(keypoints);
					iv.setDescriptors(descriptors);
					final Bitmap bmp = Bitmap.createBitmap(rgb.cols(), 
													rgb.rows(), 
													Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(rgb, bmp);
					mHandler.post(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							iv.setImageBitmap(bmp);
						}
					});
					break;
				}
			}
			mTouched = false;
		}
		//検出されたkeypointを表示
		final KeyPoint keyPointArray[] = keypoints.toArray();
		Scalar color = new Scalar(0xFF0000);
		for(KeyPoint key:keyPointArray){
		    Core.circle(inputFrame, key.pt, 10, color);
		}
		if(keyPointArray.length < 4) return inputFrame;
		
		int bestImage = -1;
		double maxInlierNum = 0.1;
		int counter = 0;
		MatOfDMatch matches = new MatOfDMatch();
		for(CapturedImageView iv:mImage){//特徴点マッチング
			if(iv.getKeypoints() == null) break;
			final KeyPoint ivKeyPointArray[] = iv.getKeypoints().toArray();
			if(ivKeyPointArray.length < 4) continue;
			//マッチング
			mMatcher.match(descriptors, iv.getDescriptors(), matches);
			final DMatch matchArray[] = matches.toArray();
			final int matchSize = matchArray.length;
			ArrayList<Point> srcArray = new ArrayList<Point>();
			ArrayList<Point> dstArray = new ArrayList<Point>();
			for(int i = 0;i < matchSize;i++){
				Point src = new Point();
				src.x = keyPointArray[matchArray[i].queryIdx].pt.x;
				src.y = keyPointArray[matchArray[i].queryIdx].pt.y;
				srcArray.add(src);
				Point dst = new Point();
				dst.x = ivKeyPointArray[matchArray[i].trainIdx].pt.x;
				dst.y = ivKeyPointArray[matchArray[i].trainIdx].pt.y;
				dstArray.add(dst);
			}
			MatOfPoint2f srcPoints = new MatOfPoint2f();
			MatOfPoint2f dstPoints = new MatOfPoint2f();
			srcPoints.fromList(srcArray);
			dstPoints.fromList(dstArray);
			//RANSACでinlier/outlierを計算
			Mat mask = new Mat();
			Calib3d.findFundamentalMat(srcPoints, dstPoints, Calib3d.RANSAC, 3.0, 0.99, mask);
			int inlierCounter = 0;
			byte [] maskArray = new byte[mask.rows()];
			mask.get(0, 0, maskArray);
			for(int i = 0;i < mask.rows();i++){
				if(maskArray[i] > 0) inlierCounter++;
			}
			final double inlier = (double)inlierCounter/(double)ivKeyPointArray.length;
			//最もinlier率の高い画像を選択
			if(inlier > maxInlierNum){
				bestImage = counter;
				maxInlierNum = inlier;
			}
			counter++;
		}
		//選択された画像にマーキング
		final int bi = bestImage;
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for(int i = 0;i < mImage.length;i++){
					if(i == bi){
						mImage[i].setBackgroundColor(0xFFFF0000);
					}
					else{
						mImage[i].setBackgroundColor(0xFF000000);
					}
				}
			}
		});

		return inputFrame;
	}
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(LOG_TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
