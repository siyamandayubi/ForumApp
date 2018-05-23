package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;

import drupal.forumapp.activities.BaseActivity;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.ForumTopicStatisticModel;
import drupal.forumapp.serverAccess.BaseAccess;
import drupal.forumapp.serverAccess.TopicsAccess;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class LoadTopicStatisticsAsyncTask extends AsyncTask<Void, Void, ForumTopicStatisticListModel> {
    private final SessionManager sessionManager;
    private final DataListener<ForumTopicStatisticListModel> dataListener;

    public LoadTopicStatisticsAsyncTask(SessionManager sessionManager, DataListener<ForumTopicStatisticListModel> dataListener) {
        this.sessionManager = sessionManager;
        this.dataListener = dataListener;
    }

    @Override
    protected ForumTopicStatisticListModel doInBackground(Void... params) {

        try {
            Gson gson = new Gson();
            ForumTopicStatisticListModel currentStatistics = new ForumTopicStatisticListModel();
            String currentStatisticsJson = sessionManager.getTopicsStatistics();
            if (!TextUtils.isEmpty(currentStatisticsJson)) {
                currentStatistics = gson.fromJson(currentStatisticsJson, currentStatistics.getClass());
            }

            return currentStatistics;
        }
        catch (Exception ex){
            Log.e("Statistics", ex.getMessage());
            Log.e("Statistics", ex.getStackTrace().toString());
            return  new ForumTopicStatisticListModel();
        }
    }

    @Override
    protected void onPostExecute( ForumTopicStatisticListModel data) {
        dataListener.onLoaded(data);
    }
}
