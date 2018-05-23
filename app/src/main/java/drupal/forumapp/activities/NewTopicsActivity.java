package drupal.forumapp.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import drupal.forumapp.asyncTasks.GenericListener;
import drupal.forumapp.model.ForumList;

import com.google.gson.Gson;

import drupal.forumapp.asyncTasks.SetTopicAsVisitedAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import drupal.forumapp.R;
import drupal.forumapp.adapters.ForumTopicsAdapter;
import drupal.forumapp.adapters.ForumTopicsForNewIdsAdapter;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.NewerTopicsLoader;
import drupal.forumapp.model.CommentModel;
import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.UserModel;
import drupal.forumapp.serverAccess.CommentsAccess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import drupal.forumapp.model.ForumTopicStatisticModel;

import android.app.NotificationManager;

import drupal.forumapp.R;
import drupal.forumapp.loaders.LoaderBaseResponse;

public class NewTopicsActivity extends ForumTopicsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_items);
    }

    @Override
    protected void initializationStep() {
        super.initializationStep();
        initialization();
    }

    @Override
    protected void initialization() {
        // Clear all notification
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();

        bindDrawerButtons();

        listInitialization();

        // start loader
        loader = (BaseLoader<?>) loaderManager.initLoader(LOADER_ID, null, this);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(loader, dataList));
        showNewItems(0);
    }

    @Override
    protected void showNewItems(int newItems) {
        TextView newTopicsTextView = (TextView) findViewById(R.id.newTopicsTextView);
        newTopicsTextView.setVisibility(View.GONE);
    }

    @Override
    protected ForumTopicsAdapter createAdapter(@LayoutRes int id, ArrayList<TopicModel> data, LoadFileAsyncTask loadFileAsyncTask, LoaderBaseResponse<ListModel<TopicModel>> loaderBaseResponse) {
        ForumList forumList = (ForumList) loaderBaseResponse.getData(LoaderBaseResponse.FORUM_KEY);
        return new ForumTopicsForNewIdsAdapter(this, R.layout.forum_topic_list_item_for_new_items, dataList, loadFileAsyncTask, forumList, loaderBaseResponse.forumTopicsStatistics, listView);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        SessionManager sessionManager = new SessionManager(this);
        String topicStatisticsJson = sessionManager.getTopicsStatistics();
        HashMap<Integer, Integer> forumsWithLastVisitedId = new HashMap<Integer, Integer>();
        if (!TextUtils.isEmpty(topicStatisticsJson)) {
            Gson gson = new Gson();
            ForumTopicStatisticListModel statistics = new ForumTopicStatisticListModel();
            statistics = gson.fromJson(topicStatisticsJson, statistics.getClass());

            for (int i = 0; i < statistics.size(); i++) {
                ForumTopicStatisticModel forumTopicStatisticModel = statistics.get(i);
                if (forumTopicStatisticModel .countNewNodes> 0) {
                    forumsWithLastVisitedId.put(forumTopicStatisticModel.tid, forumTopicStatisticModel.maxVisitedNid);
                }
            }
        }

        return new NewerTopicsLoader(this, forumsWithLastVisitedId);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        super.onLoadFinished(loader, data);

        if (dataList.size() > 0) {
            TopicModel maxIdTopic = null;
            for (TopicModel topic : dataList) {
                if (maxIdTopic == null || maxIdTopic.id < topic.id) {
                    maxIdTopic = topic;
                }
            }
            SessionManager sessionManager = new SessionManager(this);
            if (maxIdTopic != null) {
                SetTopicAsVisitedAsyncTask setTopicAsVisitedAsyncTask = new SetTopicAsVisitedAsyncTask(sessionManager, new GenericListener() {
                    @Override
                    public void onFinished() {

                    }
                });
                setTopicAsVisitedAsyncTask.execute(maxIdTopic);
            }
        }
    }
}
