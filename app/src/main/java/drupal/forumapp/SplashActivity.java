package drupal.forumapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import drupal.forumapp.activities.ForumsActivity;
import drupal.forumapp.activities.ServerSettingActivity;
import drupal.forumapp.domain.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionManager manager = new SessionManager(this);
        if (!manager.serverSettingsIsDone()){
            Intent intent = new Intent(this, ServerSettingActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, ForumsActivity.class);
            startActivity(intent);
        }
    }
}
