package drupal.forumapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import drupal.forumapp.serverAccess.ForumAccess;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.R;
import com.google.gson.Gson;
import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.domain.SessionManager;
import android.widget.Button;

public class EditForumActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText bodyEditText;
    private boolean isInAddMode;
    private ForumModel forumModel;
    private SaveHandlerTask saveHandlerTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_forum);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titleEditText = (EditText)findViewById(R.id.titleText);
        bodyEditText = (EditText)findViewById(R.id.bodyText);

        isInAddMode = savedInstanceState.getBoolean("InAddMode");
        if (!isInAddMode){
            Gson gson = new Gson();
            String forumJson = savedInstanceState.getString("forum");
            forumModel = gson.fromJson(forumJson, ForumModel.class);
            titleEditText.setText(forumModel.name);
            bodyEditText.setText(HtmlHelper.fromHtml(forumModel.description));
        }

        final Context context = this;
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SessionManager sessionManager = new SessionManager(context);
                SaveHandlerTask saveHandlerTask = new SaveHandlerTask(new ForumAccess(sessionManager));
                saveHandlerTask.execute();
            }
        });
        //saveHandlerTask = new SaveHandlerTask
    }

    private class SaveHandlerTask extends AsyncTask<ForumModel, Void, String> {
        private ForumAccess forumAccess;

        public SaveHandlerTask(ForumAccess forumAccess) {
            this.forumAccess = forumAccess;
        }

        @Override
        protected String doInBackground(ForumModel... forum) {
            try {
            } catch (Exception e) {
                Thread.interrupted();
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != "done") {
                titleEditText.setError(result);
            } else {
            }
        }
    }
}
