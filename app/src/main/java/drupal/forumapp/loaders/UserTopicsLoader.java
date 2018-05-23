package drupal.forumapp.loaders;

import android.content.Context;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.TopicsAccess;

/**
 * Created by serva on 12/19/2017.
 */

public class UserTopicsLoader extends TopicsLoader {
    private  int uid;

    public UserTopicsLoader(Context ctx, int uid) {
        super(ctx, 0);
        this.uid = uid;
    }

    @Override
    protected String getCacheId(){
        return "USER_TOPICS_LOADER";
    }

    @Override
    protected LoaderBaseResponse<ListModel<TopicModel>> fetchData(int pageId){
   		LoaderBaseResponse<ListModel<TopicModel>> output = new LoaderBaseResponse<ListModel<TopicModel>>();
       TopicsAccess topicAccess = new TopicsAccess(new SessionManager(this.getContext()));
	   output.mainOutput = topicAccess.getUserTopics(uid, pageId, PAGE_SIZE);
	   return output;
    }
}
