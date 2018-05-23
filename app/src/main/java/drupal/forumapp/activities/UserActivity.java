package drupal.forumapp.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import drupal.forumapp.R;
import drupal.forumapp.adapters.ForumTopicsAdapter;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.UserTopicsLoader;
import drupal.forumapp.model.CommentModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.UserModel;
import drupal.forumapp.serverAccess.CommentsAccess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import drupal.forumapp.loaders.LoaderBaseResponse;
import drupal.forumapp.domain.SessionManager;

public class UserActivity extends ListBaseActivity<TopicModel> implements LoaderManager.LoaderCallbacks {
    private final int LOADER_ID = 100;

    protected UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

    }

    @Override
    protected void initializationStep() {
        super.initializationStep();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();

        bindDrawerButtons();

        listInitialization();

        ScrollActivityInitializer scrollActivityInitializer = new ScrollActivityInitializer(this, toolbar, listView).invoke();
        CollapsingToolbarLayout collapsingToolbarLayout = scrollActivityInitializer.getCollapsingToolbarLayout();

        // get user from Intent
        Gson gson = new Gson();
        Bundle bundle = getIntent().getExtras();
        String userJson = bundle.getString("user");

        if (TextUtils.isEmpty(userJson)) {
            Log.e("UserActivity", "There were no user in the received bundle");
            return;
        }

        userModel = gson.fromJson(userJson, UserModel.class);

        TextView activityTitleTextView = (TextView) findViewById(R.id.activityTitleTextView);
        activityTitleTextView.setText(userModel.name);
        activityTitleTextView.setVisibility(View.VISIBLE);

        // user name
        TextView userTextView = (TextView) findViewById(R.id.usernameTextView);
        userTextView.setText(userModel.name);

        // user image
        if (userModel.picture != null) {
            LoadFileAsyncTask loadFileAsyncTask = new LoadFileAsyncTask(this);
            FileInfo fileInfo = new FileInfo();
            fileInfo.filename = userModel.picture.filename;
            fileInfo.url = userModel.picture.url;
            fileInfo.uid = userModel.uid;
            loadFileAsyncTask.execute(new FileInfo[]{fileInfo});
            final View userIconTextView = findViewById(R.id.userIconTextView);
            final ImageView userImageView = (ImageView) findViewById(R.id.userImageView);

            loadFileAsyncTask.setListener(new LoadFileTaskListener() {
                public void onFileAvailable(FileInfo file) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.localPath);
                    userImageView.setImageBitmap(bitmap);
                    userIconTextView.setVisibility(View.GONE);
                    userImageView.setVisibility(View.VISIBLE);
                }
            });
        }

        // start loader
        loader = (BaseLoader<?>) getLoaderManager().initLoader(LOADER_ID, null, this);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(loader, dataList));

        //drawer
        TextView drawerIcon1 = (TextView) findViewById(R.id.drawerIcon1);
        if (drawerIcon1 != null) {
            drawerIcon1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSlideState) {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
            });
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Bundle bundle = getIntent().getExtras();
        int forumId = bundle.getInt("forumId");
        return new UserTopicsLoader(this, userModel.uid);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        LoaderBaseResponse<ListModel<TopicModel>> loaderBaseResponse = (LoaderBaseResponse<ListModel<TopicModel>>) data;
        ListModel<TopicModel> topicsModel = loaderBaseResponse.mainOutput;

        LoadFileAsyncTask loadFileAsyncTask = new LoadFileAsyncTask(this);
        try {

            final SessionManager sessionManager = new SessionManager(this);
            GetUserCommentsTask getUserCommentsTask = new GetUserCommentsTask(new CommentsAccess(sessionManager), userModel.uid);
            getUserCommentsTask.execute();

            // hide progrss bar show listView
            View progressBar = findViewById(R.id.progressBarLayout);
            progressBar.setVisibility(View.INVISIBLE);
            View listViewLayout = findViewById(R.id.listViewLayout);
            listViewLayout.setVisibility(View.VISIBLE);
            loadMoreProgressBar.setVisibility(View.GONE);

            BaseLoader<?> baseLoader = (BaseLoader<?>) loader;
            int totalCount = baseLoader.getTotalCount();
            TextView totalPostsTextView = (TextView) findViewById(R.id.totalPostsTextView);
            totalPostsTextView.setText(totalCount + " " + getString(R.string.user_posts));

            // config adapter and listview
            dataList.addAll(topicsModel.data);

            View noItemExists = findViewById(R.id.noItemLayout);
            if (dataList.size() > 0) {
                noItemExists.setVisibility(View.GONE);
            } else {
                noItemExists.setVisibility(View.VISIBLE);
            }

            final Context context = this;
            if (adapter == null) {
                adapter = new ForumTopicsAdapter(this, R.layout.forum_topic_list_item, dataList, loadFileAsyncTask, null, loaderBaseResponse.forumTopicsStatistics, listView, null);
                adapter.setDataSelectedListener(new DataSelectedListener<TopicModel>() {
                    public void onSelect(Integer[] indexes, TopicModel[] selectedData) {
                        refreshMenus(indexes, selectedData, context);
                        TextView selectedItemsTextView = (TextView) findViewById(R.id.selectedItemsTextView);
                        selectedItemsTextView.setText(selectedItems.length + " Selected");
                    }
                });
                listView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

            // load user icons
            ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
            for (TopicModel topic : topicsModel.data) {
                if (topic.picture != null) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.filename = topic.picture.filename;
                    fileInfo.url = topic.picture.url;
                    fileInfo.uid = topic.uuid;
                    fileInfos.add(fileInfo);
                }
            }

            loadFileAsyncTask.setListener(new LoadFileTaskListener(){

                @Override
                public void onFileAvailable(FileInfo file) {
                    for(TopicModel topic : dataList){
                        if (topic.uuid == file.uid){
                            topic.localUserImagePath = file.localPath;
                        }
                    }
                }
            });

            loadFileAsyncTask.execute(fileInfos.toArray(new FileInfo[0]));

            // hide spinner of Refresh layout
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
            //adapter.addAll(forumList.forums.values());
            //adapter.notify();
        } catch (Exception ex) {
            Log.e("onLoadFinished", ex.getMessage());
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        dataList.clear();
    }


    protected Class<TopicModel> getType() {
        TopicModel t = new TopicModel();
        return (Class<TopicModel>) t.getClass();
    }

    protected Intent getAddIntent() {
        Intent intent = new Intent(this, EditTopicActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putBoolean("InAddMode", true);
        intent.putExtras(bundle);
        return intent;
    }

    @NonNull
    protected Intent getEditIntent() {
        Intent intent = new Intent(this, EditTopicActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("topic", gson.toJson(selectedItems[0]));
        bundle.putBoolean("InAddMode", false);
        intent.putExtras(bundle);
        return intent;
    }


    private class GetUserCommentsTask extends AsyncTask<Void, Void, ListModel<CommentModel>> {
        private CommentsAccess commentsAccess;
        private int uid;

        public GetUserCommentsTask(CommentsAccess commentsAccess, int uid) {
            this.commentsAccess = commentsAccess;
            this.uid = uid;
        }

        @Override
        protected ListModel<CommentModel> doInBackground(Void... voids) {
            ListModel<CommentModel> response = null;
            try {
                response = commentsAccess.getUserComments(this.uid, 0);
            } catch (Exception e) {
                Log.e("GetUserCommentsTask", e.getMessage());
                Log.e("GetUserCommentsTask", e.getStackTrace().toString());
            }

            return response;
        }

        @Override
        protected void onPostExecute(ListModel<CommentModel> data) {
            TextView totalCommentsTextView = (TextView) findViewById(R.id.totalCommentsTextView);
            totalCommentsTextView.setText(data.count + " " + getString(R.string.user_comments));
        }
    }
}
