package drupal.forumapp.activities;

import drupal.forumapp.asyncTasks.DataListener;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import drupal.forumapp.adapters.ForumTopicsAdapter;

import drupal.forumapp.R;
import drupal.forumapp.asyncTasks.DeleteAsyncTask;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.asyncTasks.LockTopicsAsyncTask;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.TopicsLoader;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.domain.AuthenticationManager;
import drupal.forumapp.asyncTasks.GenericListener;
import drupal.forumapp.serverAccess.TopicsAccess;
import drupal.forumapp.loaders.LoaderBaseResponse;

public class ForumTopicsActivity extends ListBaseActivity<TopicModel> implements LoaderManager.LoaderCallbacks {
    protected final int LOADER_ID = 100;
    static final int ADD_COMMENT_REQUEST = 3;
    private AuthenticationManager authenticationManager;
    private ForumModel forumModel;

    protected Class<TopicModel> getType() {
        TopicModel t = new TopicModel();
        return (Class<TopicModel>) t.getClass();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialize sence
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_forums);
    }

    @Override
    protected void initializationStep() {
        super.initializationStep();
        authenticationManager = new AuthenticationManager(this);
        initialization();
        setupNewTopicsClickListener();
    }

    protected void initialization() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();

        bindDrawerButtons();

        listInitialization();

        // get forum from Intent
        Gson gson = new Gson();
        Bundle bundle = getIntent().getExtras();
        String forumJson = bundle.getString("forum");

        if (TextUtils.isEmpty(forumJson)) {
            Log.e("ForumTopicsActivity", "There were no forum entry in the received bundle");
        }

        forumModel = gson.fromJson(forumJson, ForumModel.class);
        setForumProperties();


        // start loader
        loader = (BaseLoader<?>) loaderManager.initLoader(LOADER_ID, null, this);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(loader, dataList));
    }

    private void setForumProperties() {
        // forum name
        TextView textView = (TextView) findViewById(R.id.forumNameTextView);
        textView.setText(forumModel.name);

        TextView postsCountTextView = (TextView)findViewById(R.id.postsNumberTextView);
        postsCountTextView.setText("" + forumModel.num_posts);

        TextView topicsCountTextView = (TextView)findViewById(R.id.topicsNumberTextView);
        topicsCountTextView.setText("" + forumModel.num_topics);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Bundle bundle = getIntent().getExtras();
        int forumId = bundle.getInt("forumId");
        boolean clearCache = bundle.containsKey("clear_cache") && bundle.getBoolean("clear_cache");
        BaseLoader<?> loader = new TopicsLoader(this, forumModel.tid);

        if (clearCache) {
            loader.clearCache();
        }

        return  loader;
    }

    @Override
    protected Boolean showSelectMenuLockIcon(TopicModel[] selectedData) {
        return (selectedData != null && selectedData.length > 0 &&
                TopicModel.COMMENTS_CLOSED != selectedData[0].comment);
    }

    @Override
    protected Boolean showSelectMenuUnLockIcon(TopicModel[] selectedData) {
        return (selectedData != null && selectedData.length > 0 &&
                TopicModel.COMMENTS_CLOSED == selectedData[0].comment);
    }

    @Override
    protected void refreshMenus(Integer[] indexes, TopicModel[] selectedData, Context context) {
        super.refreshMenus(indexes, selectedData, context);
        boolean notAuthorized = false;

        if (selectedData != null && selectedData.length > 0) {

            for (TopicModel topicModel : selectedData) {
                if (!authenticationManager.canEditTopic(topicModel)) {
                    notAuthorized = true;
                    break;
                }
            }
        }

        if (notAuthorized) {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            selectMenuLockButton.setVisibility(View.GONE);
            selectMenuUnLockButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        LoaderBaseResponse<ListModel<TopicModel>> loaderBaseResponse = (LoaderBaseResponse<ListModel<TopicModel>>) data;
        ListModel<TopicModel> topicsModel = loaderBaseResponse != null ? loaderBaseResponse.mainOutput : null;
        try {
            // hide progrss bar show listView
            View progressBar = findViewById(R.id.progressBarLayout);
            progressBar.setVisibility(View.INVISIBLE);
            View listViewLayout = findViewById(R.id.listViewLayout);
            listViewLayout.setVisibility(View.VISIBLE);
            loadMoreProgressBar.setVisibility(View.GONE);

            if (topicsModel != null) {
                // config adapter and listview
                dataList.addAll(topicsModel.data);
            }

            View noItemExists = findViewById(R.id.noItemLayout);
            if (dataList.size() > 0) {
                noItemExists.setVisibility(View.GONE);
            } else {
                noItemExists.setVisibility(View.VISIBLE);
            }

            LoadFileAsyncTask loadFileAsyncTask = new LoadFileAsyncTask (this);
            final Context context = this;
            if (adapter == null) {
                adapter = createAdapter(R.layout.forum_topic_list_item, dataList, loadFileAsyncTask, loaderBaseResponse);
                adapter.setDataSelectedListener(new DataSelectedListener<TopicModel>() {
                    public void onSelect(Integer[] indexes, TopicModel[] selectedData) {
                        refreshMenus(indexes, selectedData, context);
                        TextView selectedItemsTextView = (TextView) findViewById(R.id.selectedItemsTextView);
                        selectedItemsTextView.setText(selectedItems.length + "");
                    }
                });
                listView.setAdapter(adapter);
            } else {
                ((ForumTopicsAdapter)adapter).setLoadFileAsyncTask(loadFileAsyncTask);
                adapter.notifyDataSetChanged();
            }

            // load user icons
            loadFiles(loadFileAsyncTask , topicsModel);

            // hide spinner of Refresh layout
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
            //adapter.addAll(forumList.forums.values());
            //adapter.notify();
        } catch (Exception ex) {
            Log.e("onLoadFinished", ex.getMessage());
        }
    }

    protected void loadFiles(LoadFileAsyncTask loadFileAsyncTask, ListModel<TopicModel> topicsModel) {
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
    }

    @Override
    protected void lockUnLockSelectedItems(final Boolean lock) {
        if (selectedItems != null && selectedItems.length > 0) {
            SessionManager sessionManager = new SessionManager(this);
            final String username = sessionManager.getUsername();

            if (TextUtils.isEmpty(username)) {
                navigateToLoginWindow.show();
            } else {
				final View lockUnLockProgressBar = findViewById(R.id.lockUnLockProgressBar);
                LockTopicsAsyncTask lockTopicsAsyncTask = new LockTopicsAsyncTask(sessionManager, lock, new GenericListener() {

                    public void onFinished() {
						lockUnLockProgressBar.setVisibility(View.GONE);
						if (!lock){
                            selectMenuLockButton.setVisibility(View.VISIBLE);
                            selectMenuUnLockButton.setVisibility(View.GONE);
						}
						else{
                            selectMenuLockButton.setVisibility(View.GONE);
                            selectMenuUnLockButton.setVisibility(View.VISIBLE);
						}
		
						adapter.notifyDataSetChanged();
                    }
                });

				lockUnLockProgressBar.setVisibility(View.VISIBLE);
				if (lock){
					selectMenuLockButton.setVisibility(View.GONE);
				}
				else{
                    selectMenuUnLockButton.setVisibility(View.GONE);
				}
                lockTopicsAsyncTask.execute(selectedItems);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ADD_REQUEST && forumModel != null) {
            forumModel.num_topics++;
            setForumProperties();
        }
    }
    protected ForumTopicsAdapter createAdapter(@LayoutRes int id, ArrayList<TopicModel> data, LoadFileAsyncTask loadFileAsyncTask, LoaderBaseResponse<ListModel<TopicModel>> loaderBaseResponse) {
        return new ForumTopicsAdapter(this, R.layout.forum_topic_list_item, dataList, loadFileAsyncTask, forumModel, loaderBaseResponse.forumTopicsStatistics, listView, new DeleteTopicClickHandler());
    }

    protected void deleteItems() {
        SessionManager sessionManager = new SessionManager(this);
        DeleteAsyncTask<TopicModel> deleteTopicsAsyncTask = new DeleteAsyncTask<TopicModel>(new TopicsAccess(sessionManager), new GenericListener() {

            public void onFinished() {
                hideDeleteDialog();
                for (TopicModel topic: selectedItems){
                    dataList.remove(topic);
                }

                forumModel.num_topics -= selectedItems.length;
                setForumProperties();
                adapter.clearSelection();
                adapter.notifyDataSetChanged();
            }
        });

        deleteTopicsAsyncTask.execute(selectedItems);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        dataList.clear();
    }

    @NonNull
    protected Intent getAddIntent() {
        Intent intent = new Intent(this, EditTopicActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("forum", gson.toJson(forumModel));
        bundle.putBoolean("InAddMode", true);
        intent.putExtras(bundle);
        return intent;
    }

    @NonNull
    protected Intent getEditIntent() {
        Intent intent = new Intent(this, EditTopicActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("forum", gson.toJson(forumModel));
        bundle.putString("topic", gson.toJson(selectedItems[0]));
        bundle.putBoolean("InAddMode", false);
        intent.putExtras(bundle);
        return intent;
    }

	protected class DeleteTopicClickHandler implements DataListener<TopicModel> {
		public void onLoaded(TopicModel topic){
			selectedItems = new TopicModel[1];
			selectedItems[0] = topic;
			showDeleteDialog();
		}
	}
}
