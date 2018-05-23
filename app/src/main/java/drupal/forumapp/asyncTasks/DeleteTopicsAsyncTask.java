package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.TopicsAccess;

/**
 * Created by serva on 11/5/2017.
 */

public class DeleteTopicsAsyncTask extends AsyncTask<TopicModel, Void, Void> {
    private final SessionManager sessionManager;
    private final GenericListener genericListener;

    public DeleteTopicsAsyncTask(SessionManager sessionManager, GenericListener genericListener) {
        this.sessionManager = sessionManager;
        this.genericListener = genericListener;
    }

    @Override
    protected Void doInBackground(TopicModel... params) {

        TopicsAccess topicAccess = new TopicsAccess(sessionManager);
        for (TopicModel topic : params) {
            topicAccess.delete(topic);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
