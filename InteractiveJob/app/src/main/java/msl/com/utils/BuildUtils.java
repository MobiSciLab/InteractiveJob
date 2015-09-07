package msl.com.utils;

import android.os.Build;

/**
 * Created by caominhvu on 6/30/15.
 */
public class BuildUtils {
    public static boolean isFromApisLevel11() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
