package drupal.forumapp.asyncTasks;

import drupal.forumapp.model.AuthenticationResponseModel;

public interface RefreshTokenTaskListener {
    public void onTokenRefreshed(AuthenticationResponseModel token);
}