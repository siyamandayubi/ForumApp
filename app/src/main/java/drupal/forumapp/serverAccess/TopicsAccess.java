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
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.NewForumTopicsResponseListModel;
import drupal.forumapp.model.NewForumTopicsResponseModel;
import drupal.forumapp.model.ResponseModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.model.TopicModel;
import android.text.TextUtils;
/**
 * Created by serva on 10/15/2017.
 */


public class TopicsAccess extends BaseAccess<TopicModel> {
    private final static String FORUM_TOPICS_LIST_ADDRESS = "api/v2.0/forumtopic/?filter[forum_tid][value]={tid}&page={page}&range={range}&sort=-created";
    private final static String USER_TOPICS_LIST_ADDRESS = "api/v2.0/forumtopic/?filter[uid][value]={uid}&page={page}&range={range}&sort=-created";
    private final static String TOPICS_BY_NID_LIST_ADDRESS = "api/v2.0/forumtopic/?filter[nid][operator]=IN&&filter[nid][value]={nid}&page={page}&range={range}&sort=-created";
    private final static String TOPICS_BY_MIN_NID_ADDRESS = "api/v2.0/forumtopic/?filter[forum_tid][value]={tid}&filter[nid][operator]=>&&filter[nid][value]={nid}&page={page}&range={range}&sort=-created";
    private final static String NEW_ITEMS_ADDRESS =  "api/v2.0/newforumtopic?filter[nid][value]={nid}&filter[tid][value]={tid}";
    private final static String EDTI_TOPIC_ADDRESS = "api/v2.0/forumtopic";
    private final static String ADD_TOPIC_ADDRESS = "api/v2.0/forumtopic";
    private final static String DELETE_TOPIC_ADDRESS = "api/v2.0/forumtopic";
    
    public TopicsAccess(SessionManager manager) {
        super(manager);
    }

    public TopicsAccess(String address) {
        super(address);
    }

    public ResponseModel<? extends TopicListModel> add(TopicModel topic){
        TopicListModel dataType = new TopicListModel();
        topic.tid = topic.forum_tid;
        ResponseModel<? extends TopicListModel> result = postData(ADD_TOPIC_ADDRESS, topic, "POST", dataType.getClass());
        return result;
    }

    public ResponseModel<String> delete(TopicModel topic){
        String method = "DELETE";
        return postData(String.format("%1$s/%2$s", DELETE_TOPIC_ADDRESS, topic.id + ""), topic, method, String.class);
    }

    public ResponseModel<? extends TopicListModel> edit(TopicModel topic){
        String method = "PUT";
        String address = String.format("%1$s/%2$s", EDTI_TOPIC_ADDRESS, topic.id + "");
        TopicListModel dataType = new TopicListModel();
        return postData(address , topic, method, dataType.getClass());
    }

    public NewForumTopicsResponseListModel getNewItems(int maxId, int tid){
        String address = NEW_ITEMS_ADDRESS;
        address = address.replace("{nid}", "" + maxId);
        address = address.replace("{tid}", "" + tid);
        NewForumTopicsResponseListModel x = new NewForumTopicsResponseListModel();
        return getData(address,  x.getClass());
    }

    public TopicListModel getTopics(int tid) {
        return getTopics(tid, 0, 10);
    }

    public TopicListModel getTopics(int tid, int page, int pageSize) {
        String address = FORUM_TOPICS_LIST_ADDRESS;
        address = address.replace("{tid}", "" + tid);
        address = address.replace("{page}", "" + (page + 1));
        address = address.replace("{range}", "" + pageSize);
        TopicListModel x = new TopicListModel();
        return getData(address,  x.getClass());
    }

    public TopicListModel getUserTopics(int uid, int page, int pageSize) {
        String address = USER_TOPICS_LIST_ADDRESS;
        address = address.replace("{uid}", "" + uid);
        address = address.replace("{page}", "" + (page + 1));
        address = address.replace("{range}", "" + pageSize);
        TopicListModel x = new TopicListModel();
        return getData(address,  x.getClass());
    }

    public TopicListModel getNewerTopics(int tid, int oldNid, int page, int pageSize) {
        String address = TOPICS_BY_MIN_NID_ADDRESS;
        address = address.replace("{tid}", "" + tid);
        address = address.replace("{nid}", "" + oldNid);
        address = address.replace("{page}", "" + (page + 1));
        address = address.replace("{range}", "" + pageSize);
        TopicListModel x = new TopicListModel();
        return getData(address,  x.getClass());
    }
    
    public TopicListModel getTopicsByIds(int[] nids, int page, int pageSize) {
        if (nids == null || nids.length == 0){
            return null;
        }
        String address = TOPICS_BY_NID_LIST_ADDRESS;
        String nidsStr = "";
        for(int id:nids){
            nidsStr += id + ",";
        }

        nidsStr = nidsStr.substring(0,nidsStr.length() - 1);
        address = address.replace("{nid}", nidsStr);
        address = address.replace("{page}", "" + (page + 1));
        address = address.replace("{range}", "" + pageSize);
        TopicListModel x = new TopicListModel();
        return getData(address,  x.getClass());
    }
}
