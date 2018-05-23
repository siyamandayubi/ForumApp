package drupal.forumapp.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public class Util {
    private static final int REFRESH_DATA_JOB_ID = 101;

    // schedule the start of the service every 10 - 30 seconds
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, RefreshDataJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(REFRESH_DATA_JOB_ID, serviceComponent);
        builder.setPeriodic(120 * 1000); // wait at least
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require  network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            if (job.getId() == REFRESH_DATA_JOB_ID) {
                jobScheduler.cancel(REFRESH_DATA_JOB_ID);
            }
        }

        jobScheduler.schedule(builder.build());
    }
}