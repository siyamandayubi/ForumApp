package drupal.forumapp.loaders;

import android.content.Context;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.serverAccess.TopicsAccess;
 import drupal.forumapp.serverAccess.ForumAccess;

/**
 * Created by serva on 12/19/2017.
 */

public class TopicsByIdsLoader extends TopicsLoader {
    private  int[] nids;

    public TopicsByIdsLoader(Context ctx, int[] nids) {
        super(ctx, 0);
        this.nids = nids;
    }

    @Override
    public LoaderBaseResponse<ListModel<TopicModel>> loadInBackground() {
        this.refreshData = true;
        return super.loadInBackground();
    }

    @Override
    protected  LoaderBaseResponse<ListModel<TopicModel>> fetchData(int pageId){
        TopicsAccess topicAccess = new TopicsAccess(new SessionManager(this.getContext()));
       ForumAccess forumAccess = new ForumAccess(new SessionManager(this.getContext()));
        LoaderBaseResponse<ListModel<TopicModel>> output = new LoaderBaseResponse<ListModel<TopicModel>>();

        output.setData(LoaderBaseResponse.FORUM_KEY, forumAccess.getForums());
		output.forumTopicsStatistics = getForumTopicsStatistics();
	    output.mainOutput = topicAccess.getTopicsByIds(nids, pageId, PAGE_SIZE);
	   return output;
   }
}
