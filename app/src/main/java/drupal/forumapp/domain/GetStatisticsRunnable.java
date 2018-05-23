package drupal.forumapp.domain;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.ForumTopicStatisticModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.NewForumTopicsResponseModel;
import drupal.forumapp.serverAccess.TopicsAccess;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;

/**
 * Created by serva on 1/20/2018.
 */

public class GetStatisticsRunnable implements Runnable {
	
	public static final int NOTIFICATION_MODE = 1;
	public static final int BROADCAST_MODE = 2;
	public static final String INTENT_NAME = "New_Items_Intent";

    private Context context;
    private  SessionManager sessionManager;
    private TopicsAccess topicsAccess;
	private int mode;
    public GetStatisticsRunnable(Context context, int mode){
        this.context = context;
        sessionManager = new SessionManager(context);
        topicsAccess = new TopicsAccess(sessionManager);
		this.mode = mode;
    }
    @Override
    public void run() {
        try {
            Gson gson = new Gson();
            ForumTopicStatisticListModel currentStatistics = new ForumTopicStatisticListModel();
            String currentStatisticsJson = sessionManager.getTopicsStatistics();
            if (!TextUtils.isEmpty(currentStatisticsJson)) {
                currentStatistics = gson.fromJson(currentStatisticsJson, currentStatistics.getClass());
            }

			ListModel<NewForumTopicsResponseModel> newForumTopicsCounts = new ListModel<NewForumTopicsResponseModel>();
            for (ForumTopicStatisticModel forumTopicStatisticModel: currentStatistics){
                if(forumTopicStatisticModel.maxVisitedNid > 0){
		            ListModel<NewForumTopicsResponseModel> receivedData = topicsAccess.getNewItems(forumTopicStatisticModel.maxVisitedNid, forumTopicStatisticModel.tid);
		            if (receivedData != null && receivedData.data.size() > 0) {
                        if (forumTopicStatisticModel.maxVisitedNid > 0) {
                            forumTopicStatisticModel.countNewNodes = receivedData.data.get(0).node_count;
                        }
                        else{
                            forumTopicStatisticModel.countNewNodes = 0;
                        }

                        forumTopicStatisticModel.maxNid = receivedData.data.get(0).node_maxid;
                    }
                    else{
		                forumTopicStatisticModel.countNewNodes = 0;
                    }
				}
            }

            currentStatisticsJson = gson.toJson(currentStatistics);
            sessionManager.persistTopicsStatistics(currentStatisticsJson);

            int newItems = 0;
            int forumsCount = 0;
            for (int i = 0; i < currentStatistics.size(); i++) {
                ForumTopicStatisticModel node = currentStatistics.get(i);
                if (node.countNewNodes > 0) {
                    forumsCount++;
                    newItems += node.countNewNodes ;
                }
            }

            if (newItems > 0) {
				if (mode == NOTIFICATION_MODE){
					NotificationHelper.createNotification(context, newItems + " new Items.", "There are " + newItems + " new items in " + forumsCount + " differnet Forums", 1);
				}
				else{
					Intent intent = new Intent(INTENT_NAME);
				    intent.putExtra("NewTopics",  newItems);

				    LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
				}
            }
        } catch (Exception ex) {
            Log.e("Service", ex.getMessage());
            Log.e("service", ex.getStackTrace().toString());
        }
    }
}
