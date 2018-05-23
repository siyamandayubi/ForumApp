package drupal.forumapp.domain;

import android.content.Context;
import android.content.SharedPreferences;

import drupal.forumapp.model.AuthenticationResponseModel;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import android.preference.PreferenceManager;
/**
 * Created by serva on 10/13/2017.
 */

public class SessionManager {

    public static final String SERVER_ROOT_ADDRESS = "ServerRootAddress";

    private Context context;
    private KeyStoreManager keyStoreManager;
    private static final String SHARED_PREFERENCES = "Settings";
    private static final String AUTHENTICATION_TOKEN = "AuthenticationToken";
    private static final String USER_NAME = "Username";
    private static final String PASSWORD = "Password";
    private static final String LAST_SYNCED_TOPIC_ID = "LastSyncedTopicId";
    private static final String LAST_SYNCED_FORUM_ID = "LastSyncedForumId";
    private static final String LAST_SYNCED_COMMENT_ID = "LastSyncedCommentId";
    private static final String FAVORITE_TOPICS = "FavoriteTopics";
    private static final String TOPICS_STATISTICS = "TopicsStatistics";    
    private static final String USER_PERMISSIONS = "UserPermissions";    
    
    public SessionManager(Context context) {
        this.context = context;
        keyStoreManager = new KeyStoreManager(this.context);
        try {
            keyStoreManager.initialize();
        } catch (Exception ex) {
            Log.e("keyStoreManager", ex.getMessage());
            Log.e("keyStoreManager", ex.getStackTrace().toString());
        }
    }

	public void clearServerRelatedData(){
		persistString(AUTHENTICATION_TOKEN, "");
		persistString(USER_NAME, "");
		persistString(PASSWORD, "");
		persistString(LAST_SYNCED_TOPIC_ID, "");
		persistString(LAST_SYNCED_FORUM_ID, "");
		persistString(LAST_SYNCED_COMMENT_ID, "");
		persistString(FAVORITE_TOPICS, "");
		persistString(TOPICS_STATISTICS, "");
		persistString(USER_PERMISSIONS, "");
	}

    public boolean serverSettingsIsDone() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String serverAddress = sharedPref.getString(SERVER_ROOT_ADDRESS, "");

        return serverAddress != "";
    }

    public String getBaseAddress() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String serverAddress = sharedPref.getString(SERVER_ROOT_ADDRESS, "");

        return serverAddress;
    }

    public Integer getLastVisitedTopicId() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Integer id = sharedPref.getInt(LAST_SYNCED_TOPIC_ID, 0);

        return id;
    }

    public Integer getLastVisitedCommentId() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Integer id = sharedPref.getInt(LAST_SYNCED_COMMENT_ID, 0);

        return id;
    }

    public Integer getLastVisitedForumId() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Integer id = sharedPref.getInt(LAST_SYNCED_FORUM_ID, 0);

        return id;
    }

    public AuthenticationResponseModel getAuthenticationToken() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String tokenStr = sharedPref.getString(AUTHENTICATION_TOKEN, "");
        if (TextUtils.isEmpty(tokenStr)) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(tokenStr, AuthenticationResponseModel.class);
    }

    public void setAuthenticationToken(AuthenticationResponseModel data) {
        Calendar givenDate = Calendar.getInstance();
        givenDate.add(Calendar.SECOND, (int) data.expires_in);
        data.expirationDateTime = givenDate.getTime();

        Log.d("time", givenDate.toString());
        Gson gson = new Gson();
        String jsonData = gson.toJson(data);
        persistString(AUTHENTICATION_TOKEN, jsonData);
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void persistUsernamePassword(String username, String password) {
        try {
            String encryptedPassword = keyStoreManager.encrypt(password);
            persistString(PASSWORD, encryptedPassword);

            String encryptedUsernamw = keyStoreManager.encrypt(username);
            persistString(USER_NAME, encryptedUsernamw);
        } catch (Exception exception) {
            Log.e("persistUsernamePassword", exception.getMessage());
            Log.e("persistUsernamePassword", exception.getStackTrace().toString());
        }
    }

    public void clearAuthentications() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(PASSWORD);
        editor.remove(USER_NAME);
        editor.remove(AUTHENTICATION_TOKEN);
        editor.commit();

    }

    public String getUsername() {
        return getEncryptedData(USER_NAME);
    }

    public String getPassword() {
        return getEncryptedData(PASSWORD);
    }

    public String getTopicsStatistics(){
        return getString(TOPICS_STATISTICS);
    }

    public void persistTopicsStatistics(String data) {
        persistString(TOPICS_STATISTICS, data);
    }

    public String getUserPermissions(){
        return getString(USER_PERMISSIONS);
    }

    public void persistUserPermissions(String data) {
        persistString(USER_PERMISSIONS, data);
    }

    public String getFavoriteTopics(){
        return getString(FAVORITE_TOPICS);
    }

    public void persistFavoriteTopics(String data) {
        persistString(FAVORITE_TOPICS, data);
    }

    public void PersistServerAddress(String url) {
        persistString(SERVER_ROOT_ADDRESS, url, true);
    }

    public void PersistLastVisitedTopicId(int id) {
        persistInteger(LAST_SYNCED_TOPIC_ID, id);
    }

    public void PersistLastVisitedCommentId(Integer id) {
        persistInteger(LAST_SYNCED_COMMENT_ID, id);
    }

    public void PersistLastVisitedForumId(Integer id) {
        persistInteger(LAST_SYNCED_FORUM_ID, id);
    }

    private String getEncryptedData(String key) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            String data = sharedPref.getString(key, "");
            if (TextUtils.isEmpty(data)) {
                return null;
            }

            return keyStoreManager.decryptString(data);
        } catch (Exception ex) {
            Log.e("getEncryptionData", ex.getMessage());
            Log.e("getEncryptionData", ex.getStackTrace().toString());
            return "";
        }
    }

    private String getString(String key){
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String data = sharedPref.getString(key, "");
 
        return data;
    }

	private void persistString(String key, String value){
		persistString(key, value, false);
	}

    private void persistString(String key, String value, Boolean getDefault) {
        SharedPreferences sharedPref = getDefault?
		PreferenceManager.getDefaultSharedPreferences(context):
		context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void persistInteger(String key, Integer value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
