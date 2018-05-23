package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;

import drupal.forumapp.activities.BaseActivity;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.BaseAccess;
import drupal.forumapp.serverAccess.TopicsAccess;
import com.google.gson.Gson;
import android.text.TextUtils;
import java.util.ArrayList;

/**
 * Created by serva on 11/5/2017.
 */

public class AddFavoriteTopicAsyncTask extends AsyncTask<TopicModel, Void, Void> {
    private final SessionManager sessionManager;
    private final GenericListener genericListener;

    public AddFavoriteTopicAsyncTask(SessionManager sessionManager, GenericListener genericListener) {
        this.sessionManager = sessionManager;
        this.genericListener = genericListener;
    }

    @Override
    protected Void doInBackground(TopicModel... params) {

        Gson gson = new Gson();
        String favoriteListJson = sessionManager.getFavoriteTopics();
        ArrayList<Integer> favorites = null;
        if (!TextUtils.isEmpty(favoriteListJson)){
            favorites = gson.fromJson(favoriteListJson, (new ArrayList<Integer>()).getClass());
        }
        else{
             favorites = new ArrayList<Integer>();
        }
        for (TopicModel topic: params) {
            if (!favorites.contains(topic.nid)){
                favorites.add(topic.nid);
            }
        }

        favoriteListJson = gson.toJson(favorites);
        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
