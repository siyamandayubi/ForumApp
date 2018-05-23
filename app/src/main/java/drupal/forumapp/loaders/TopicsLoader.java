package drupal.forumapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.serverAccess.TopicsAccess;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.TopicListModel;
import drupal.forumapp.App;

/**
 * Created by serva on 10/17/2017.
 */

public class TopicsLoader extends StatisticsBaseLoader<ListModel<TopicModel>> {
    private int forumId;

    public TopicsLoader(Context ctx, int forumId) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
        this.forumId = forumId;
    }

    protected String getCacheId() {

        return "TOPICS_LOADER" + this.forumId;
    }

    @Override
    public LoaderBaseResponse<ListModel<TopicModel>> loadInBackground() {
        int page = getNextPage();
        App app = (App) this.getContext().getApplicationContext();
        if (!refreshData) {
            Object cachedData = app.getCacheItem(getCacheId());
            if (cachedData != null) {
                LoaderBaseResponse<ListModel<TopicModel>> returnValue = (LoaderBaseResponse<ListModel<TopicModel>>) cachedData;
                this.totalCount = returnValue.mainOutput.count;
                setNextPage(page + 1);
                return  returnValue;
            }
        }

        LoaderBaseResponse<ListModel<TopicModel>> topicListModel = fetchData(page);
        if (topicListModel == null || topicListModel.mainOutput == null) {
            totalCount = 0;
            return null;
        }

        totalCount = topicListModel.mainOutput.count;

        if(page == 0) {
            app.putCacheItem(getCacheId(), topicListModel);
        }
        else{

        }

        setNextPage(page + 1);

        return topicListModel;
    }

    protected LoaderBaseResponse<ListModel<TopicModel>> fetchData(int page) {
        LoaderBaseResponse<ListModel<TopicModel>> output = new LoaderBaseResponse<ListModel<TopicModel>>();
        TopicsAccess topicAccess = new TopicsAccess(new SessionManager(this.getContext()));
        output.mainOutput = topicAccess.getTopics(forumId, page, PAGE_SIZE);
        output.forumTopicsStatistics = getForumTopicsStatistics();
        return output;
    }
}
