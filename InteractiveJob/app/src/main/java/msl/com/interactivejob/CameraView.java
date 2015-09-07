package msl.com.interactivejob;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class CameraView extends JavaCameraView  {

    private static final String TAG = "Sample::Tutorial3View";
    private String mPictureFileName;
    Context mContext;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAutoFocusHandler = new Handler(context.getMainLooper());
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    private Handler mAutoFocusHandler;
    // Mimic continuous auto-focusing
    private Camera.AutoFocusCallback mAutoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if(mCamera != null && !((Activity) mContext).isFinishing())
                        triggerAutoFocus();
                }
            }, 400);
        }
    };
    private boolean isAutofocusable = false;

    public void triggerAutoFocus() {
        if (mCamera != null && isAutofocusable && !((Activity) mContext).isFinishing()) {
            mCamera.autoFocus(mAutoFocusCB);
        } else if(!((Activity) mContext).isFinishing()){
            mAutoFocusHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    triggerAutoFocus();
                }
            }, 400);
        }
    }
    public void destroy() {
        mAutoFocusHandler.removeCallbacksAndMessages(null);
    }
}
