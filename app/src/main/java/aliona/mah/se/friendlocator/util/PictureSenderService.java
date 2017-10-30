package aliona.mah.se.friendlocator.util;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by aliona on 2017-10-22.
 */

public class PictureSenderService extends IntentService {
    private boolean serviceRunning;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public PictureSenderService(String name) {
        super(name);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO: get the ip and port + bitmap to upload here
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    public void onDestroy() {
        Log.d("onDestroy","Picture Service down");
        serviceRunning = false;
        super.onDestroy();
    }
}
