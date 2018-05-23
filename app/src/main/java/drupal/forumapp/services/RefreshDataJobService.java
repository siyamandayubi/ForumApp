package drupal.forumapp.services;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Handler;

import drupal.forumapp.domain.GetStatisticsRunnable;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.serverAccess.TopicsAccess;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class RefreshDataJobService extends JobService {
    public RefreshDataJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        HandlerThread handlerThread = new HandlerThread("RefreshDataServiceHandlerThread");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        final SessionManager sessionManager = new SessionManager(this);
        final TopicsAccess topicsAccess = new TopicsAccess(sessionManager);
        final Context context = this;
        handler.post(new GetStatisticsRunnable(context, GetStatisticsRunnable.NOTIFICATION_MODE) {
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
