package drupal.forumapp.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;

import android.widget.TextView;

import drupal.forumapp.asyncTasks.AddRemoveFavoriteTopicAsyncTask;
import drupal.forumapp.asyncTasks.DeleteAsyncTask;
import drupal.forumapp.asyncTasks.GenericListener;
import drupal.forumapp.asyncTasks.LoadFileAsyncTask;

//import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import drupal.forumapp.adapters.CommentsAdapter;
import drupal.forumapp.asyncTasks.LockTopicsAsyncTask;
import drupal.forumapp.asyncTasks.SetTopicAsVisitedAsyncTask;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.domain.DateHelper;
import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;

import drupal.forumapp.R;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.domain.AuthenticationManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.CommentsLoader;
import drupal.forumapp.model.FavoriteTopicModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.CommentModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.ForumModel;

import android.text.TextUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import drupal.forumapp.model.UserModel;
import drupal.forumapp.serverAccess.CommentsAccess;
import drupal.forumapp.serverAccess.TopicsAccess;

import android.support.design.widget.CollapsingToolbarLayout;

public class TopicCommentsActivity extends ListBaseActivity<CommentModel> implements LoaderManager.LoaderCallbacks {

    private final int LOADER_ID = 100;
    private TopicModel topicModel;
    private ForumModel forumModel;
    private String forumJson;
    private List<FavoriteTopicModel> favorites;
    private boolean topicIsSelectedForDelete;
    private ScrollActivityInitializer scrollActivityInitializer;
    private AuthenticationManager authenticationManager;
    private View lockTopicIcon;
    private View unlockTopicIcon;
    private View topicLockUnLockProgressBar;
    private View topicLockButton;
    private View topicUnlockButton;
    private TextView addButtonOnTopTextView;
    private TextView getAddButtonOnTopDisabledTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.addNewItemsToBeginOfTheList = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_comments);

        Gson gson = new Gson();
        Bundle bundle = getIntent().getExtras();
        String topicJson = bundle.getString("topic");
        forumJson = bundle.getString("forum");

        if (TextUtils.isEmpty(topicJson)) {
            Log.e("TopicCommentsActivity", "There were no topic entry in the received bundle");
        }

        if (TextUtils.isEmpty(forumJson)) {
            Log.e("TopicCommentsActivity", "There were no forum entry in the received bundle");
        }

        forumModel = gson.fromJson(forumJson, ForumModel.class);
        topicModel = gson.fromJson(topicJson, TopicModel.class);
        setActivityTitle();
    }


    @Override
    protected void initializationStep() {
        //ViewGroup mainContentContainer = (ViewGroup)findViewById(R.id.mainContentContainer);
        //getLayoutInflater().inflate(R.layout.content_topic_comments,mainContentContainer,true);
        super.initializationStep();
        setupNewTopicsClickListener();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        authenticationManager = new AuthenticationManager(this);
        final SessionManager sessionManager = new SessionManager(this);

        setSupportActionBar(toolbar);
        drawerInitialization();
        bindDrawerButtons();

        final Activity context = this;
        listInitialization();

        View header = getLayoutInflater().inflate(R.layout.comments_header, null);
        listView.addHeaderView(header);

        //scrollActivityInitializer = new ScrollActivityInitializer(context, toolbar, listView).invoke();

        SetTopicAsVisitedAsyncTask setTopicAsVisitedAsyncTask = new SetTopicAsVisitedAsyncTask(sessionManager, new GenericListener() {
            @Override
            public void onFinished() {

            }
        });

        setTopicAsVisitedAsyncTask.execute(topicModel);

        addButtonOnTopTextView = (TextView) findViewById(R.id.addCommentTopTextView);
        getAddButtonOnTopDisabledTextView = (TextView) findViewById(R.id.addCommentTopTextDisabledView);
        addButtonOnTopTextView.setOnClickListener(new AddButtonClickListener(this));

        // username
        TextView usernameView = (TextView) findViewById(R.id.topicUsernameTextView);
        usernameView.setText(HtmlHelper.fromHtml(topicModel.name));

        //TextView addCommentButton = (TextView)findViewById(R.id.addCommentButton);
        //addCommentButton.setOnClickListener(new AddButtonClickListener(this));
        setTopicProperties(header);
        topicLockUnLockProgressBar = findViewById(R.id.topicLockUnLockProgressBar);

        TextView editIconTextView = (TextView) findViewById(R.id.editIconTextView);
        String username = sessionManager.getUsername();
        if (!authenticationManager.canEditTopic(topicModel)) {
            if (!TextUtils.isEmpty(username)) {
                editIconTextView.setVisibility(View.GONE);
            }
        } else {
            editIconTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SessionManager sessionManager = new SessionManager(context);
                    final String username = sessionManager.getUsername();

                    if (TextUtils.isEmpty(username)) {
                        navigateToLoginWindow.show();
                    } else {
                        Intent intent = new Intent(context, EditTopicActivity.class);
                        Bundle bundle = new Bundle();
                        Gson gson = new Gson();
                        bundle.putString("forum", forumJson);
                        bundle.putString("topic", gson.toJson(topicModel));
                        bundle.putBoolean("InAddMode", false);
                        intent.putExtras(bundle);
                        context.startActivityForResult(intent, EDIT_REQUEST);
                    }
                }
            });
        }

        final TextView favoriteTextView = (TextView) findViewById(R.id.favoriteIconTextView);
        loadFavorites();
        setFavoriteTextViewColor(favoriteTextView, topicModel.nid);
        favoriteTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                FavoriteTopicModel found = FavoriteTopicModel.find(favorites, topicModel.nid);
                final Boolean addMode = found == null;

                AddRemoveFavoriteTopicAsyncTask addRemoveFavoriteTopicAsyncTask = new AddRemoveFavoriteTopicAsyncTask(sessionManager, addMode, new GenericListener() {

                    public void onFinished() {
                        loadFavorites();
                        setFavoriteTextViewColor(favoriteTextView, topicModel.nid);
                    }
                });

                addRemoveFavoriteTopicAsyncTask.execute(topicModel);
            }
        });

        LoadFileAsyncTask loadFileAsyncTask = new LoadFileAsyncTask(this);
        FileInfo fileInfo = new FileInfo();
        fileInfo.filename = topicModel.picture.filename;
        fileInfo.url = topicModel.picture.url;
        fileInfo.uid = topicModel.uuid;
        loadFileAsyncTask.execute(new FileInfo[]{fileInfo});
        final View userIconTextView = findViewById(R.id.topicUserIconTextView);
        final ImageView userImageView = (ImageView) findViewById(R.id.topicUserImageView);
        userIconTextView.setOnClickListener(new UserClickListener(this));
        userImageView.setOnClickListener(new UserClickListener(this));

        loadFileAsyncTask.setListener(new LoadFileTaskListener() {
            public void onFileAvailable(FileInfo file) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.localPath);
                userImageView.setImageBitmap(bitmap);
                userIconTextView.setVisibility(View.GONE);
                userImageView.setVisibility(View.VISIBLE);
            }
        });

        loader = (BaseLoader<?>) loaderManager.initLoader(LOADER_ID, null, this);

        final BaseLoader<?> tLoader = loader;
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(tLoader, dataList));

        this.topicIsSelectedForDelete = false;
        TextView deleteIconTextView = (TextView) findViewById(R.id.deleteIconTextView);
        if (!authenticationManager.canDeleteTopic(topicModel)) {
            if (!TextUtils.isEmpty(username)) {
                deleteIconTextView.setVisibility(View.GONE);
            }
        } else {
            deleteIconTextView.setOnClickListener(new DeleteTopicButtonClickListener(this));
        }

        View topicLockButton = findViewById(R.id.topicLockButton);
        View topicUnlockButton = findViewById(R.id.topicUnlockButton);
        View topicLockUnLockProgressBar = findViewById(R.id.topicLockUnLockProgressBar);
        final Boolean canEditTopic = authenticationManager.canEditTopic(topicModel);
        topicLockButton.setOnClickListener(new LockUnlockClickListener(canEditTopic, true, this).invoke());

        topicUnlockButton.setOnClickListener(new LockUnlockClickListener(canEditTopic, false, this).invoke());

        initializeDeleteConfirmationPopup();
    }

    private void setTopicProperties(View header) {

        lockTopicIcon = findViewById(R.id.lockTopicIcon);
        unlockTopicIcon = findViewById(R.id.unlockTopicIcon);
        topicLockButton = findViewById(R.id.topicLockButton);
        topicUnlockButton = findViewById(R.id.topicUnlockButton);

        if (topicModel.comment == TopicModel.COMMENTS_OPEN) {
            unlockTopicIcon.setVisibility(View.VISIBLE);
            lockTopicIcon.setVisibility(View.GONE);
            topicLockButton.setVisibility(View.VISIBLE);
            topicUnlockButton.setVisibility(View.GONE);
            addButtonOnTopTextView.setVisibility(View.VISIBLE);
            getAddButtonOnTopDisabledTextView.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        } else {
            unlockTopicIcon.setVisibility(View.GONE);
            lockTopicIcon.setVisibility(View.VISIBLE);
            topicLockButton.setVisibility(View.GONE);
            topicUnlockButton.setVisibility(View.VISIBLE);
            addButtonOnTopTextView.setVisibility(View.GONE);
            getAddButtonOnTopDisabledTextView.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
        }
        Boolean canEditTopic = authenticationManager.canEditTopic(topicModel);
        if (!canEditTopic) {
            topicLockButton.setVisibility(View.GONE);
            topicUnlockButton.setVisibility(View.GONE);
        }

        // title
        final TextView topicTitleTextView = (TextView) findViewById(R.id.topicTitleTextView);
        topicTitleTextView.setText(topicModel.title);

        setActivityTitle();

        // body
        TextView topicBodyTextView = (TextView) header.findViewById(R.id.topicBodyTextView);
        topicBodyTextView.setText(HtmlHelper.fromHtml(topicModel.body));

        // comments count
        TextView commentsCountView = (TextView) header.findViewById(R.id.commentsCountTextView);
        TextView commentsCountInToolbarView = (TextView) header.findViewById(R.id.commentsCountTextView);
        String commentsStr = getResources().getString(R.string.topic_comments_count);
        commentsCountView.setText(topicModel.comment_count + " " + commentsStr);
        commentsCountInToolbarView.setText(topicModel.comment_count + " " + commentsStr);

        if (topicModel.created > 0) {
            TextView createdDateTextView = (TextView) header.findViewById(R.id.createdDateTextView);
            createdDateTextView.setText(DateHelper.format(topicModel.created));
        }

        if (topicModel.changed > 0 && topicModel.changed != topicModel.created) {
            TextView changedDateTextView = (TextView) header.findViewById(R.id.modifiedDateTextView);
            changedDateTextView.setText(DateHelper.format(topicModel.changed));
        }
    }

    private void setActivityTitle() {
        final TextView activityTitleTextView = (TextView) findViewById(R.id.activityTitleTextView);
        String shortTitle = topicModel.title.length() > 20 ? topicModel.title.substring(0, 20) + "..." : topicModel.title;
        activityTitleTextView.setText(shortTitle);
    }

    @Override
    protected void refreshMenus(Integer[] indexes, CommentModel[] selectedData, Context context) {
        super.refreshMenus(indexes, selectedData, context);
        boolean notAuthorized = false;
        if (selectedData != null) {
            for (CommentModel commentModel : selectedData) {
                if (!authenticationManager.canEditComment(commentModel)) {
                    notAuthorized = true;
                    break;
                }
            }
        }

        if (notAuthorized) {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void cancelDeleteOperation() {
        topicIsSelectedForDelete = false;
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

    protected void setFavoriteTextViewColor(TextView favoriteTextView, int nid) {
        FavoriteTopicModel found = FavoriteTopicModel.find(favorites, nid);
        if (found != null) {
            favoriteTextView.setTextColor(this.getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else {
            favoriteTextView.setTextColor(this.getResources().getColor(R.color.unSelectedText, this.getTheme()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle bundle = intent.getExtras();
        String dataType = bundle.getString("data-type");
        if (dataType == null || dataType.isEmpty()) {
            super.onActivityResult(resultCode, resultCode, intent);
        }
        else if (dataType.equalsIgnoreCase("comment")) {
            super.onActivityResult(requestCode, resultCode, intent);
            if (requestCode == ADD_REQUEST) {
                topicModel.comment_count++;
            }
        } else if (dataType.equalsIgnoreCase("topic")) {
            Gson gson = new Gson();
            topicModel = gson.fromJson(bundle.getString("data"), TopicModel.class);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Bundle bundle = getIntent().getExtras();
        int nid = bundle.getInt("topicId");
        return new CommentsLoader(this, nid);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        ListModel<CommentModel> commentsModel = (ListModel<CommentModel>) data;
        LoadFileAsyncTask loadFileAsyncTask = new LoadFileAsyncTask(this);
        try {
            View progressBar = findViewById(R.id.progressBarLayout);
            progressBar.setVisibility(View.INVISIBLE);
            View swipRefreshLayout = findViewById(R.id.swipRefreshLayout);
            swipRefreshLayout.setVisibility(View.VISIBLE);
            loadMoreProgressBar.setVisibility(View.GONE);

            dataList.addAll(commentsModel.data);

            View noItemExists = findViewById(R.id.noItemLayout);
            TextView commentsCountView = (TextView) findViewById(R.id.commentsCountTextView);
            if (dataList.size() > 0) {
                noItemExists.setVisibility(View.GONE);
                //  commentsCountView.setVisibility(View.VISIBLE);
            } else {
                commentsCountView.setVisibility(View.GONE);
                String commentsStr = getResources().getString(R.string.topic_comments_count);
                commentsCountView.setText("0 " + commentsStr);
                noItemExists.setVisibility(View.VISIBLE);
            }

            final Context context = this;
            if (adapter == null) {
                adapter = new CommentsAdapter(this, R.layout.comment_item, dataList, loadFileAsyncTask, topicModel);
                adapter.setDataSelectedListener(new DataSelectedListener<CommentModel>() {
                    public void onSelect(Integer[] indexes, CommentModel[] selectedData) {
                        refreshMenus(indexes, selectedData, context);
                        TextView selectedItemsTextView = (TextView) findViewById(R.id.selectedItemsTextView);
                        selectedItemsTextView.setText(selectedItems.length + " Selected");
                    }
                });

                listView.setAdapter(adapter);
                listView.smoothScrollToPosition(adapter.getCount());
            } else {
                adapter.notifyDataSetChanged();
            }

            ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
            for (CommentModel comment : commentsModel.data) {
                if (comment.picture != null) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.filename = comment.picture.filename;
                    fileInfo.url = comment.picture.url;
                    fileInfo.uid = comment.uuid;
                    fileInfos.add(fileInfo);
                }
            }
            loadFileAsyncTask.execute(fileInfos.toArray(new FileInfo[0]));

            loadFileAsyncTask.setListener(new LoadFileTaskListener(){

                @Override
                public void onFileAvailable(FileInfo file) {
                    for(CommentModel comment: dataList){
                        if (comment.uuid == file.uid){
                            comment.localUserImagePath = file.localPath;
                        }
                    }
                }
            });

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

    }

    protected void deleteItems() {
        SessionManager sessionManager = new SessionManager(this);
        if (!topicIsSelectedForDelete) {
            DeleteAsyncTask<CommentModel> deleteAsyncTask = new DeleteAsyncTask<CommentModel>(new CommentsAccess(sessionManager), new GenericListener() {

                public void onFinished() {
                    hideDeleteDialog();
                    for (CommentModel comment : selectedItems) {
                        dataList.remove(comment);
                    }

                    adapter.clearSelection();
                    adapter.notifyDataSetChanged();
                }
            });

            deleteAsyncTask.execute(selectedItems);
        } else {
            topicIsSelectedForDelete = false;
            final Context context = this;
            DeleteAsyncTask<TopicModel> deleteAsyncTask = new DeleteAsyncTask<TopicModel>(new TopicsAccess(sessionManager), new GenericListener() {

                public void onFinished() {
                    Intent intent = new Intent(context, ForumTopicsActivity.class);
                    Bundle bundle = new Bundle();
                    forumModel.num_topics--;
                    Gson gson = new Gson();
                    forumJson = gson.toJson(forumModel);
                    bundle.putString("forum", forumJson);
                    bundle.putBoolean("clear_cache", true);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            deleteAsyncTask.execute(topicModel);
        }
    }

    @Override
    protected Class getType() {
        CommentModel comment = new CommentModel();
        return comment.getClass();
    }

    @Override
    protected Intent getEditIntent() {
        Intent intent = new Intent(this, EditCommentActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("topic", gson.toJson(topicModel));
        bundle.putString("comment", gson.toJson(selectedItems[0]));
        bundle.putBoolean("InAddMode", false);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected Intent getAddIntent() {
        Intent intent = new Intent(this, EditCommentActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("topic", gson.toJson(topicModel));
        bundle.putString("forum", forumJson);
        bundle.putBoolean("InAddMode", true);
        intent.putExtras(bundle);
        return intent;
    }

    protected class DeleteTopicButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public DeleteTopicButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            SessionManager sessionManager = new SessionManager(context);
            final String username = sessionManager.getUsername();

            if (TextUtils.isEmpty(username)) {
                navigateToLoginWindow.show();
            } else {
                topicIsSelectedForDelete = true;
                showDeleteDialog();
            }
        }
    }

    public class UserClickListener implements View.OnClickListener {

        private final Context context;

        public UserClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            UserModel userModel = new UserModel();
            userModel.uid = topicModel.uuid;
            userModel.name = topicModel.name;
            userModel.picture = topicModel.picture;

            Intent intent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String userJson = gson.toJson(userModel);
            bundle.putString("user", userJson);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    private class LockUnlockClickListener {
        private final Boolean canEditTopic;
        private final Boolean lock;
        private final Context context;

        public LockUnlockClickListener(Boolean canEditTopic, Boolean lock, Context context) {
            this.canEditTopic = canEditTopic;
            this.lock = lock;
            this.context = context;
        }

        public View.OnClickListener invoke() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!canEditTopic) {
                        return;
                    }

                    topicLockUnLockProgressBar.setVisibility(View.VISIBLE);
                    topicLockButton.setVisibility(View.GONE);
                    topicUnlockButton.setVisibility(View.GONE);
                    SessionManager sessionManager = new SessionManager(context);
                    LockTopicsAsyncTask lockTopicsAsyncTask = new LockTopicsAsyncTask(sessionManager, lock, new GenericListener() {

                        public void onFinished() {
                            topicLockUnLockProgressBar.setVisibility(View.GONE);
                            setTopicProperties(listView);
                        }
                    });
                    lockTopicsAsyncTask.execute(topicModel);
                }
            };
        }
    }
}
