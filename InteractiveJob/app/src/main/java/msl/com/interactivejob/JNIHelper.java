package msl.com.interactivejob;

/**
 * Created by caominhvu on 9/4/15.
 */
public class JNIHelper {
    static {
        System.loadLibrary("img_recognition");
    }

    private volatile static JNIHelper _instance;

    public static JNIHelper getInstanceDC() {
        if (_instance == null) {
            synchronized (JNIHelper.class) {
                if (_instance == null) {
                    _instance = new JNIHelper();
                }
            }
        }
        return _instance;
    }


    public native float[] test(String classifier, String vocabularies, long imgAddr, float threshold);
}
