package drupal.forumapp.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import drupal.forumapp.R;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.serverAccess.ForumAccess;

public class ServerSettingActivity extends AppCompatActivity {
    protected EditText editText;
    protected ProgressBar progressBar;
    protected SessionManager sessionManager;
    protected Context currentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_server_setting);
        editText = (EditText) findViewById(R.id.urlEditText);
        editText.setText("http://10.0.2.2:8088");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.currentContext = this;
        LinearLayout layout = (LinearLayout) findViewById(R.id.toolbarLayout);
        //layout.setBackgroundColor(Color.parseColor("#ffaacc"));
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = editText.getText().toString();
                if (url.isEmpty()) {
                    editText.setError("Please provide a valid url");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                SaveHandlerTask saveHandlerTask = new SaveHandlerTask(new ForumAccess(url));
                saveHandlerTask.execute(url);
            }
        });
    }

    private class SaveHandlerTask extends AsyncTask<String, Void, String> {
        private ForumAccess forumAccess;

        public SaveHandlerTask(ForumAccess forumAccess)
        {
            this.forumAccess = forumAccess;
        }

        @Override
        protected String doInBackground(String... url) {
            try {
                ForumList result = forumAccess.getForums();
                if (result != null && result.forums != null) {
                    return "done";
                } else {
                    return "Error connecting to the server";
                }
            } catch (Exception e) {
                Thread.interrupted();
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String result) {

            progressBar.setVisibility(View.INVISIBLE);
            if (result != "done") {
                editText.setError(result);
            } else {
                String url = editText.getText().toString();
                SessionManager sessionManager = new SessionManager(currentContext);
                sessionManager.PersistServerAddress(url);

                Intent intent = new Intent(currentContext, ForumsActivity.class);
                currentContext.startActivity(intent);
            }
        }
    }
}
