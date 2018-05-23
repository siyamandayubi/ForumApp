package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.ForumTopicStatisticModel;

import com.google.gson.Gson;

import android.text.TextUtils;

/**
 * Created by serva on 11/5/2017.
 */

public class SetTopicAsVisitedAsyncTask extends AsyncTask<TopicModel, Void, Void> {
    private final SessionManager sessionManager;
    private final GenericListener genericListener;

    public SetTopicAsVisitedAsyncTask(SessionManager sessionManager, GenericListener genericListener) {
        this.sessionManager = sessionManager;
        this.genericListener = genericListener;
    }

    @Override
    protected Void doInBackground(TopicModel... params) {

        Gson gson = new Gson();
        ForumTopicStatisticListModel currentStatistics = new ForumTopicStatisticListModel();
        String currentStatisticsJson = sessionManager.getTopicsStatistics();
        if (!TextUtils.isEmpty(currentStatisticsJson)){
            currentStatistics = gson.fromJson(currentStatisticsJson, currentStatistics.getClass());
        }

        for (TopicModel topic: params) {

			ForumTopicStatisticModel currentNode = null;
            for(ForumTopicStatisticModel node: currentStatistics){
                if (node.tid == topic.forum_tid){
					currentNode = node;
					break;
                }
            }

			if (currentNode == null){
                currentNode = new ForumTopicStatisticModel();
                currentNode.tid = topic.forum_tid;
                currentNode.maxVisitedNid = 0;
                currentNode.countVisitedNodes = 0;
                currentStatistics.add(currentNode);
				currentNode.countNewNodes = 0;	
			}

            if (currentNode.maxVisitedNid < topic.nid){
               currentNode.maxVisitedNid = topic.nid;
               if (topic.nid == currentNode.maxNid){
  				  currentNode.countNewNodes = 0;
                  currentNode.maxVisitedNid = topic.nid;
               }
               else if (topic.nid > currentNode.maxVisitedNid && topic.nid < currentNode.maxNid){
                  currentNode.countVisitedNodes++;
                  currentNode.maxVisitedNid = topic.nid;
				  currentNode.countNewNodes--;
               }
            }

        }

        currentStatisticsJson = gson.toJson(currentStatistics);
        sessionManager.persistTopicsStatistics(currentStatisticsJson);
        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
