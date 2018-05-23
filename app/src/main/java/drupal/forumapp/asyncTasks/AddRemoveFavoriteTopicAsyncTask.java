package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;

import drupal.forumapp.activities.BaseActivity;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.model.FavoriteTopicModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.BaseAccess;
import drupal.forumapp.serverAccess.TopicsAccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by serva on 11/5/2017.
 */

public class AddRemoveFavoriteTopicAsyncTask extends AsyncTask<TopicModel, Void, Void> {
    private final SessionManager sessionManager;
    private final GenericListener genericListener;
    private final Boolean addMode;

    public AddRemoveFavoriteTopicAsyncTask(SessionManager sessionManager, Boolean addMode, GenericListener genericListener) {
        this.sessionManager = sessionManager;
        this.genericListener = genericListener;
        this.addMode = addMode;
    }

    @Override
    protected Void doInBackground(TopicModel... params) {

        Moshi moshi = new Moshi.Builder().build();
        Type type = Types.newParameterizedType(List.class, FavoriteTopicModel.class);
        JsonAdapter<List<FavoriteTopicModel>> adapter = moshi.adapter(type);

        String favoriteListJson = sessionManager.getFavoriteTopics();
        List<FavoriteTopicModel> favorites = new ArrayList<FavoriteTopicModel>();
        if (!TextUtils.isEmpty(favoriteListJson)) {
            try {
                favorites = adapter.fromJson(favoriteListJson);
            } catch (IOException e) {
                Log.e("loadFavorites", e.getMessage());
                Log.e("loadFavorites", e.getStackTrace().toString());
            }
        }

        for (TopicModel topic : params) {
            FavoriteTopicModel found = FavoriteTopicModel.find(favorites, topic.nid);

            if (addMode) {
                if (found == null) {
                    FavoriteTopicModel newItem = new FavoriteTopicModel();
                    newItem.nid = topic.nid;
                    favorites.add(newItem);
                }
            } else {
                if (found != null) {
                    favorites.remove(found);
                }
            }
        }

        favoriteListJson = adapter.toJson(favorites);
        sessionManager.persistFavoriteTopics(favoriteListJson);
        return null;
    }

    @Override
    protected void onPostExecute(Void dummy) {
        genericListener.onFinished();
    }
}
