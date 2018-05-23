package drupal.forumapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import drupal.forumapp.domain.GetStatisticsRunnable;

public class RefreshDataService extends Service {
    private static final int DELAYS = 120000;
    public Context context = this;
    public Handler handler = null;
    public Runnable runnable = null;

    public RefreshDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("Thread name", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();

        handler = new Handler(serviceLooper);
        runnable = new StatisticsDomainWrapper(context);
        handler.post(runnable);
    }

    public class StatisticsDomainWrapper extends GetStatisticsRunnable {
        public StatisticsDomainWrapper(Context context) {
            super(context, GetStatisticsRunnable.BROADCAST_MODE);
        }

        @Override
        public void run() {
            super.run();
            handler.postDelayed(runnable, DELAYS);
        }
    }
}
