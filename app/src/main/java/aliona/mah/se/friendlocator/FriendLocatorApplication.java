package aliona.mah.se.friendlocator;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by aliona on 2017-10-24.
 */

public class FriendLocatorApplication extends Application {


    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
