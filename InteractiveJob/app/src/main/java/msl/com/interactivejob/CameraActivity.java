package msl.com.interactivejob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import msl.com.httpclient.MSConfig;
import msl.com.utils.IOUtils;

public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "CameraActivity";
    private CameraView mOpenCvCameraView;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;

    private String mClassifier, mVocabularies;
    private JSONObject mClassifierMap;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    if (MSConfig.DEBUG) {
                        Log.i(TAG, "OpenCV loaded successfully");
                    }
                    System.loadLibrary("opencv_java3");
                    System.loadLibrary("img_recognition");

                    InputStream classifierStream = getResources().openRawResource(R.raw.classifier);
                    InputStream vocabulariesStream = getResources().openRawResource(R.raw.vocabulary);
                    InputStream classifierMapStream = getResources().openRawResource(R.raw.map);
                    try {
                        mClassifier = IOUtils.toString(classifierStream);
                        mVocabularies = IOUtils.toString(vocabulariesStream);
                        mClassifierMap = new JSONObject(IOUtils.toString(classifierMapStream));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                    mOpenCvCameraView.triggerAutoFocus();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);



    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.destroy();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }


    @Override
    public  Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Imgproc.GaussianBlur(mGray, mIntermediateMat, new Size(7, 7), 0);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3), new Point(1, 1));
        Imgproc.dilate(mIntermediateMat, mIntermediateMat, kernel);
        Imgproc.Canny(mIntermediateMat, mIntermediateMat, 80, 100);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        Imgproc.findContours(mIntermediateMat, contours, hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1, new Point(0, 0));
        Rect rect[] = new Rect[contours.size()]; //List out all bounding rect
        for (int i = 0; i < contours.size(); i++) {
            rect[i] = Imgproc.boundingRect(contours.get(i));
        }
        hierachy.release();
        contours.clear();


        //Post process merge nearby bouding box
        Mat mask = Mat.zeros(mRgba.size(), CvType.CV_8UC1);
        Size scaleFactor = new Size(15, 15);
        for (int i = 0; i < rect.length; i++) {
            Rect box = new Rect(rect[i].x, rect[i].y, (int) (rect[i].width + scaleFactor.width), (int) (rect[i].height + scaleFactor.height));
            Imgproc.rectangle(mask, box.tl(), box.br(), new Scalar(255, 0, 0), 10);
        }

        List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
        Mat hierachy2 = new Mat();
        Imgproc.findContours(mask, contours2, hierachy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1, new Point(0, 0));
        hierachy2.release();
        if (contours2.size() > 0) {
            Rect roi[] = new Rect[contours2.size()];
            for (int i = 0; i < contours2.size(); i++) {
                if (Imgproc.contourArea(contours2.get(i)) > 5000) {
                    roi[i] = Imgproc.boundingRect(contours2.get(i));
                    Imgproc.rectangle(mRgba, roi[i].tl(), roi[i].br(), new Scalar(255, 0, 0), 4);
                }
            }

            if (!mIsProcessing) {
                mIsProcessing = true;
                mCandidate.clear();
                for (int i = 0; i < roi.length; i++) {
                    if (mGray != null && roi[i] != null)
                        mCandidate.add(new Mat(mGray, roi[i]));

                }
                Log.i(TAG, "Total candidates............." + mCandidate.size());
                if (mCandidate.size() > 0) {
                    mHandler.post(mImgRegRunable);
                }
            }
        }

        return mRgba;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public static final String COMPANY_NAME = "COMPANY_NAME";
    List<Mat> mCandidate = new ArrayList<Mat>();
    Handler mHandler = new Handler(Looper.getMainLooper());
    boolean mIsProcessing = false;
    private Runnable mImgRegRunable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "Start executing...");
            int minDistance = 32767;
            float classId = -1;
            for(int i =0; i< mCandidate.size(); i++) {
                float[] result = JNIHelper.getInstanceDC().test(mClassifier, mVocabularies, mCandidate.get(i).getNativeObjAddr(), 0.035f);
                if (result[0] != -1 && result[0] < 0.2f) {
                    if (minDistance > result[1]) {
                        classId = result[0];
                    }
                }
            }
            String companyName = null;
            try {
                if(classId != -1) {
                    companyName = mClassifierMap.getString(String.valueOf((int) classId));
                    Log.i(TAG, mClassifierMap.getString(String.valueOf((int) classId)));
                    companyName = companyName.split("/")[2];
                    if (companyName.contains("__")) {
                        companyName = companyName.split("__")[0];
                    }
                    companyName = companyName.replace(".jpg", "");
                    companyName = companyName.replace(".png", "");
                    companyName = companyName.replace(".gif", "");
                    Toast.makeText(CameraActivity.this, companyName, Toast.LENGTH_LONG).show();

                    setResult(Activity.RESULT_OK);
                    Intent intent = new Intent();
                    intent.putExtra(COMPANY_NAME, companyName);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIsProcessing = false;
        }
    };
}
