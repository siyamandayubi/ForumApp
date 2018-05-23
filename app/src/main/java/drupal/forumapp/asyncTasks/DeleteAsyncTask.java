package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;

import drupal.forumapp.activities.BaseActivity;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.BaseAccess;
import drupal.forumapp.serverAccess.TopicsAccess;

/**
 * Created by serva on 11/5/2017.
 */

public class DeleteAsyncTask<T extends Object> extends AsyncTask<T, Void, Void> {
    private final BaseAccess<T> dataAccess;
    private final GenericListener genericListener;

    public DeleteAsyncTask(BaseAccess<T> dataAccess, GenericListener genericListener) {
        this.dataAccess = dataAccess;
        this.genericListener = genericListener;
    }

    @Override
    protected Void doInBackground(T... params) {

        for (T data: params) {
            dataAccess.delete(data);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
