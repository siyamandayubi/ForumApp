package drupal.forumapp.serverAccess;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ResponseModel;

/**
 * Created by serva on 10/15/2017.
 */
public class ForumAccess extends BaseAccess<ForumModel> {
    private final static String FORUM_BY_TID_LIST_ADDRESS = "api/v1.0/forum/?filter[tid][operator]=IN&&filter[tid][value]={tid}&sort=-created";
    private final static String FORUM_LIST_ADDRESS = "api/v1.0/forum";

    public ForumAccess(SessionManager manager) {
        super(manager);
    }

    public ForumAccess(String address) {
        super(address);
    }

    public ForumList getForums() {
        return getData(FORUM_LIST_ADDRESS, ForumList.class);
    }

	public ForumList getForumsByIds(int[] tids) {
        if (tids == null || tids.length == 0){
            return null;
        }

        String address = FORUM_BY_TID_LIST_ADDRESS;
        String tidsStr = "";
        for(int id: tids){
            tidsStr += id + ",";
        }

        tidsStr = tidsStr.substring(0, tidsStr.length() - 1);
        address = address.replace("{tid}", tidsStr);
        ForumList x = new ForumList();
        return getData(address,  x.getClass());
    }

    public void update(ForumModel forum){

    }

    public void add(ForumModel forum){

    }

    @Override
    public ResponseModel<String> delete(ForumModel forum){
        return  null;
    }
}
