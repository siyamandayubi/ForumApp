package drupal.forumapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import drupal.forumapp.App;
import drupal.forumapp.loaders.LoaderBaseResponse;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.serverAccess.ForumAccess;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.ResponseModel;
import drupal.forumapp.R;

import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.domain.SessionManager;

import android.widget.Button;

import drupal.forumapp.R;
import drupal.forumapp.serverAccess.TopicsAccess;

public class EditTopicActivity extends BaseActivity {
    private EditText titleEditText;
    private EditText bodyEditText;
    private boolean isInAddMode;
    private ForumModel forumModel;
    private TopicModel topicModel;
    private SaveHandlerTask saveHandlerTask;
    private ProgressBar progressBar;
    private TextView saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_topic);
    }

    @Override
    protected void initializationStep() {
        super.initializationStep();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();
        bindDrawerButtons();
        setupNewTopicsClickListener();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        titleEditText = (EditText) findViewById(R.id.titleText);
        bodyEditText = (EditText) findViewById(R.id.bodyText);

        Gson gson = new Gson();
        Bundle bundle = getIntent().getExtras();
        String forumJson = bundle.getString("forum");
        forumModel = gson.fromJson(forumJson, ForumModel.class);
        isInAddMode = bundle.getBoolean("InAddMode");

        TextView forumNameTextView = (TextView) findViewById(R.id.forumTitleTextView);
        forumNameTextView.setText(forumModel.name);

        TextView toolbarTitleTextView = (TextView) findViewById(R.id.toolbarTitleTextView);

        if (!isInAddMode) {
            toolbarTitleTextView.setText("Edit Topic");
            String topicJson = bundle.getString("topic");
            topicModel = gson.fromJson(topicJson, TopicModel.class);
            titleEditText.setText(topicModel.title);
            bodyEditText.setText(HtmlHelper.fromHtml(topicModel.body));
        } else {
            toolbarTitleTextView.setText("Add a new Topic");
        }

        final Activity context = this;
        saveButton = (TextView) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SessionManager sessionManager = new SessionManager(context);
                SaveHandlerTask saveHandlerTask = new SaveHandlerTask(new TopicsAccess(sessionManager));
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);

                View activeView = context.getCurrentFocus();
                if (activeView != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(activeView.getWindowToken(), 0);
                }

                if (isInAddMode) {
                    topicModel = new TopicModel();
                    topicModel.comment = TopicModel.COMMENTS_OPEN;
                }

                topicModel.forum_tid = forumModel.tid;
                topicModel.title = titleEditText.getText().toString();
                topicModel.body = bodyEditText.getText().toString();
                saveHandlerTask.execute(topicModel);
            }
        });
    }

    private class SaveHandlerTask extends AsyncTask<TopicModel, Void, ResponseModel<? extends TopicListModel>> {
        private TopicsAccess topicAccess;

        public SaveHandlerTask(TopicsAccess topicAccess) {
            this.topicAccess = topicAccess;
        }

        @Override
        protected ResponseModel<? extends TopicListModel> doInBackground(TopicModel... topic) {
            ResponseModel<? extends TopicListModel> response = null;
            try {
                if (isInAddMode) {
                    response = topicAccess.add(topic[0]);
                } else {
                    response = topicAccess.edit(topic[0]);
                }
            } catch (Exception e) {
                Log.e("EditTopicActivity", e.getMessage());
                Log.e("EditTopicActivity", e.getStackTrace().toString());
            }

            return response;
        }

        @Override
        protected void onPostExecute(ResponseModel<? extends TopicListModel> response) {

            progressBar.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);

            if (response.statusCode != 200) {
                titleEditText.setError("Error in persisting data");
                saveButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                Gson gson = new Gson();
                Intent data = new Intent();
                TopicModel changedData = response.result.data.get(0);
                changedData.comment = topicModel.comment;
                changedData.comment_count = topicModel.comment_count;
                changedData.last_comment_timestamp = topicModel.last_comment_timestamp;
                changedData.last_comment_uid = topicModel.last_comment_uid;
                data.putExtra("data", gson.toJson(changedData));
                data.putExtra("data-type", "topic");
                // Activity finished ok, return the data
                setResult(RESULT_OK, data);

                App app = (App) getApplication();
                LoaderBaseResponse<ListModel<TopicModel>> cachedData = (LoaderBaseResponse<ListModel<TopicModel>>) app.getCacheItem("TOPICS_LOADER" + forumModel.tid);
                if (cachedData != null) {
                    if (isInAddMode) {
                        cachedData.mainOutput.data.add(0, changedData);
                    } else {
                        for (int i = 0; i < cachedData.mainOutput.data.size(); i++) {
                            if (cachedData.mainOutput.data.get(i).nid == changedData.nid) {
                                cachedData.mainOutput.data.set(i, changedData);
                            }
                        }
                    }
                }
                finish();
            }
        }
    }
}
