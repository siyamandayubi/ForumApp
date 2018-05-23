package drupal.forumapp.asyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.serverAccess.AuthenticationAccess;

/**
 * Created by serva on 11/5/2017.
 */

public class RefreshTokenAsyncTask extends AsyncTask<AuthenticationResponseModel, Void, AuthenticationResponseModel> {
    private final SessionManager sessionManager;
    private RefreshTokenTaskListener refreshTokenTaskListener;

    public RefreshTokenAsyncTask(SessionManager sessionManager, RefreshTokenTaskListener refreshTokenTaskListener) {
        this.sessionManager = sessionManager;
        this.refreshTokenTaskListener = refreshTokenTaskListener;
    }

    @Override
    protected AuthenticationResponseModel doInBackground(AuthenticationResponseModel... params) {
        AuthenticationAccess authenticationAccess = new AuthenticationAccess(sessionManager);
        AuthenticationResponseModel old = params[0];
        AuthenticationResponseModel newToken = null;
        try {
            boolean expired = (old == null || old.expirationDateTime.before(Calendar.getInstance().getTime()));
            if (expired) {
                newToken = authenticationAccess.refresh(old.refresh_token);
            }
            else{
                newToken = old;
            }

            if(expired && newToken == null){
                String username = sessionManager.getUsername();
                String password = sessionManager.getPassword();

                newToken = authenticationAccess.authenticate(username, password);
            }

            return newToken;
        } catch (IOException ex) {
            Log.e("RefreshTokenAsyncTask", ex.getMessage());
            Log.e("RefreshTokenAsyncTask", ex.getStackTrace().toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(AuthenticationResponseModel authenticationResponseModel) {
        if (authenticationResponseModel != null) {
            refreshTokenTaskListener.onTokenRefreshed(authenticationResponseModel);
        }
    }
}
