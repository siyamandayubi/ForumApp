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

public class LockTopicsAsyncTask extends AsyncTask<TopicModel, Void, Void> {
    private final SessionManager sessionManager;
    private final GenericListener genericListener;
	private final Boolean lock;

    public LockTopicsAsyncTask(SessionManager sessionManager, Boolean lock, GenericListener genericListener) {
        this.sessionManager = sessionManager;
		this.lock = lock;
        this.genericListener = genericListener;
    }

    @Override
    protected Void doInBackground(TopicModel... params) {

        TopicsAccess topicAccess = new TopicsAccess(sessionManager);
        for (TopicModel topic : params) {
			if (this.lock){
				topic.comment = TopicModel.COMMENTS_CLOSED;
			}
			else{
				topic.comment = TopicModel.COMMENTS_OPEN;			
			}

            topicAccess.edit(topic);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
