package drupal.forumapp.loaders;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.ForumAccess;
import drupal.forumapp.serverAccess.TopicsAccess;

/**
 * Created by serva on 12/19/2017.
 */

public class NewerTopicsLoader extends TopicsLoader {
    private HashMap<Integer, Integer> forumsWithLastVisitedId;

    public NewerTopicsLoader(Context ctx, HashMap<Integer, Integer> forumsWithLastVisitedId) {
        super(ctx, 0);
        this.forumsWithLastVisitedId = forumsWithLastVisitedId;
    }

    @Override
    public LoaderBaseResponse<ListModel<TopicModel>> loadInBackground() {
        this.refreshData = true;
        return super.loadInBackground();
    }

    @Override
    protected LoaderBaseResponse<ListModel<TopicModel>>  fetchData(int pageId){
        LoaderBaseResponse<ListModel<TopicModel>> output = new LoaderBaseResponse<ListModel<TopicModel>>();
        ForumAccess forumAccess = new ForumAccess(new SessionManager(this.getContext()));
        TopicsAccess topicAccess = new TopicsAccess(new SessionManager(this.getContext()));
        output.setData(LoaderBaseResponse.FORUM_KEY, forumAccess.getForums());
 		output.forumTopicsStatistics = getForumTopicsStatistics();
        output.mainOutput = new ListModel<TopicModel>();
        output.mainOutput.data = new ArrayList<TopicModel>();
        for(Integer key: forumsWithLastVisitedId.keySet()) {
            ListModel<TopicModel> result = topicAccess.getNewerTopics(key, forumsWithLastVisitedId.get(key), pageId, PAGE_SIZE);
            if (result != null) {
                output.mainOutput.data.addAll(result.data);
                output.mainOutput.count += result.count;
            }
        }
        return output;
    }
}
