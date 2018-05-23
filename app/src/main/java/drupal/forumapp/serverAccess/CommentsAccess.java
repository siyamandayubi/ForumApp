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
import drupal.forumapp.model.CommentListModel;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.CommentModel;
import drupal.forumapp.model.ResponseModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.model.TopicModel;

/**
 * Created by serva on 10/15/2017.
 */


public class CommentsAccess extends BaseAccess<CommentModel> {
    private final static String TOPIC_COMMENTS_LIST_ADDRESS = "api/v1.0/comments?filter[nid][value]={nid}&page={page}";
    private final static String ADD_COMMENT_ADDRESS =  "api/v1.0/comments";
    private final static String EDIT_COMMENT_ADDRESS = "api/v1.0/comments";
    private final static String USER_COMMENTS_LIST_ADDRESS = "api/v1.0/comments/?filter[uuid][value]={uid}&page={page}&sort=-created";
    
    public CommentsAccess(SessionManager manager) {
        super(manager);
    }

    public CommentsAccess(String address) {
        super(address);
    }

    public ResponseModel<? extends CommentListModel> add(CommentModel comment){
        CommentListModel dataType = new CommentListModel();
        ResponseModel<? extends CommentListModel> result = postData(ADD_COMMENT_ADDRESS, comment, "POST", dataType.getClass());
        return result;
    }

    public ResponseModel<String> delete(CommentModel comment){
        String method = "DELETE";
        return postData(String.format("%1$s/%2$s", EDIT_COMMENT_ADDRESS, comment.cid + ""), comment, method, String.class);
    }

    public ResponseModel<? extends CommentListModel> edit(CommentModel comment){
        String method = "PUT";
        String address = String.format("%1$s/%2$s", EDIT_COMMENT_ADDRESS, comment.cid + "");
        CommentListModel dataType = new CommentListModel();
        return postData(address , comment, method, dataType.getClass());
    }

    public ListModel<CommentModel> getComments(int nid) {
        return getComments(nid, 0);
    }

    public ListModel<CommentModel> getComments(int nid, int page) {
        String address = TOPIC_COMMENTS_LIST_ADDRESS;
        address = address.replace("{nid}", "" + nid);
        address = address.replace("{page}", "" + (page + 1));
        CommentListModel x = new CommentListModel();
        return getData(address, x.getClass());
    }

    public ListModel<CommentModel> getUserComments(int uid, int page) {
        String address = USER_COMMENTS_LIST_ADDRESS;
        address = address.replace("{uid}", "" + uid);
        address = address.replace("{page}", "" + (page + 1));
        CommentListModel x = new CommentListModel();
        return getData(address, x.getClass());
    }
}
