package drupal.forumapp.activities;

import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.TopicsByIdsLoader;

import drupal.forumapp.R;
import drupal.forumapp.model.FavoriteTopicModel;

public class FavoritesActivity extends ForumTopicsActivity {

    private final int LOADER_ID = 101;
    private List<FavoriteTopicModel> favorites;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
     }

    @Override
    protected void initializationStep() {
        super.initializationStep();
        initialization();
    }

    @Override
    protected void initialization() {
        loadFavorites();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();
        setupNewTopicsClickListener();

        bindDrawerButtons();

        listInitialization();

        // start loader
        loader = (BaseLoader<?>) loaderManager.initLoader(LOADER_ID, null, this);
        
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(loader, dataList));

        TextView activityTitleTextView = (TextView)findViewById(R.id.activityTitleTextView);
        activityTitleTextView.setText(R.string.title_activity_favorites);
    }
    
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Bundle bundle = getIntent().getExtras();
        int[] nids = new int[favorites.size()];
        for(int i = 0; i <favorites.size(); i++){
            nids[i] = favorites.get(i).nid;
        }

        return new TopicsByIdsLoader(this, nids);
    }
    
    protected void loadFavorites() {
        SessionManager sessionManager = new SessionManager(this);
        String favoritesJson = sessionManager.getFavoriteTopics();
        favorites = new ArrayList<FavoriteTopicModel>();
        if (!TextUtils.isEmpty(favoritesJson)) {
            Moshi moshi = new Moshi.Builder().build();
            Type type = Types.newParameterizedType(List.class, FavoriteTopicModel.class);
            JsonAdapter<List<FavoriteTopicModel>> adapter = moshi.adapter(type);

            try {
                favorites = adapter.fromJson(favoritesJson);
            } catch (IOException e) {
                Log.e("loadFavorites", e.getMessage());
                Log.e("loadFavorites", e.getStackTrace().toString());
            }
        }
    }
}
