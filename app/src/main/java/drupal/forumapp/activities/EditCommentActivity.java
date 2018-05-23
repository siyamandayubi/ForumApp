package drupal.forumapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import drupal.forumapp.model.ForumModel;
import drupal.forumapp.serverAccess.ForumAccess;
import drupal.forumapp.model.CommentModel;
import drupal.forumapp.model.CommentListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.ResponseModel;
import drupal.forumapp.R;

import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.domain.SessionManager;

import android.widget.Button;

import drupal.forumapp.R;
import drupal.forumapp.serverAccess.CommentsAccess;

public class EditCommentActivity extends BaseActivity {
    private EditText titleEditText;
    private EditText bodyEditText;
    private ProgressBar progressBar;
    private TextView saveButton;
    private boolean isInAddMode;
    private CommentModel commentModel;
    private TopicModel topicModel;
    private SaveHandlerTask saveHandlerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);
     }

    @Override
    protected void initializationStep(){
        super.initializationStep();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerInitialization();
        bindDrawerButtons();
        setupNewTopicsClickListener();


        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        titleEditText = (EditText) findViewById(R.id.titleText);
        bodyEditText = (EditText) findViewById(R.id.bodyText);

        Gson gson = new Gson();
        Bundle bundle = getIntent().getExtras();
        String forumJson = bundle.getString("topic");
        topicModel = gson.fromJson(forumJson, TopicModel.class);
        isInAddMode = bundle.getBoolean("InAddMode");

        TextView topicNameTextView = (TextView)findViewById(R.id.titleTextView);
        topicNameTextView.setText(topicModel.title);

        TextView toolbarTitleTextView = (TextView)findViewById(R.id.toolbarTitleTextView);

        if (!isInAddMode) {
            toolbarTitleTextView.setText("Edit Comment");
            String commentJson = bundle.getString("comment");
            commentModel = gson.fromJson(commentJson, CommentModel.class);
            titleEditText.setText(commentModel.label);
            bodyEditText.setText(HtmlHelper.fromHtml(commentModel.comment_body));
        }else{
            toolbarTitleTextView.setText("Add a new Comment");
        }

        final Activity context = this;
        saveButton = (TextView) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SessionManager sessionManager = new SessionManager(context);
                SaveHandlerTask saveHandlerTask = new SaveHandlerTask(new CommentsAccess(sessionManager));
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);

                View activeView = context.getCurrentFocus();
                if (activeView != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(activeView.getWindowToken(), 0);
                }

                if (isInAddMode) {
                    commentModel = new CommentModel();
                }

                commentModel.nid = topicModel.nid;
                commentModel.label = titleEditText.getText().toString();
                commentModel.comment_body = bodyEditText.getText().toString();
                saveHandlerTask.execute(commentModel);
            }
        });
    }

    private class SaveHandlerTask extends AsyncTask<CommentModel, Void, ResponseModel<? extends CommentListModel>> {
        private CommentsAccess commentsAccess;

        public SaveHandlerTask(CommentsAccess commentsAccess) {
            this.commentsAccess = commentsAccess;
        }

        @Override
        protected ResponseModel<? extends CommentListModel> doInBackground(CommentModel... comment) {
            ResponseModel<? extends CommentListModel> response = null;
            try {
                if (isInAddMode) {
                    response = commentsAccess.add(comment[0]);
                } else {
                    response = commentsAccess.edit(comment[0]);
                }
            } catch (Exception e) {
                Log.e("EditCommentActivity", e.getMessage());
                Log.e("EditCommentActivity", e.getStackTrace().toString());
            }

            return response;
        }

        @Override
        protected void onPostExecute (ResponseModel<? extends CommentListModel> response) {

            progressBar.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            if (response.statusCode != 200) {
                titleEditText.setError("Error in persisting data");
            } else {
                Gson gson = new Gson();
                Intent data = new Intent();
                data.putExtra("data", gson.toJson(response.result.data.get(0)));
                data.putExtra("data-type", "comment");
                // Activity finished ok, return the data
                setResult(RESULT_OK, data);
                finish();
            }
        }

    }
}