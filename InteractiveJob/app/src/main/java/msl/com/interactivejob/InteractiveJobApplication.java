package msl.com.interactivejob;

import android.app.Application;

/**
 * Created by caominhvu on 8/5/15.
 */
public class InteractiveJobApplication extends Application {
    private volatile static InteractiveJobApplication uniqueInstance;
    public InteractiveJobApplication() {
        super();
        uniqueInstance = this;
    }

    public static InteractiveJobApplication getInstance() {
        return uniqueInstance;
    }
}
