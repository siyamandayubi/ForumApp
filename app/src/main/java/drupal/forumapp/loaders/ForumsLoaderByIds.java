package drupal.forumapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.serverAccess.ForumAccess;
import drupal.forumapp.services.RefreshDataService;

/**
 * Created by serva on 10/17/2017.
 */

public class ForumsLoaderByIds extends StatisticsBaseLoader<ForumList> {

    private int[] tids;
    public ForumsLoaderByIds(Context ctx, int[] tids) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
        this.tids = tids;
    }

    @Override
    public LoaderBaseResponse<ForumList> loadInBackground() {
  		LoaderBaseResponse<ForumList> output = new LoaderBaseResponse<ForumList>();
        ForumAccess forumAccess = new ForumAccess(new SessionManager(this.getContext()));
 		output.forumTopicsStatistics = getForumTopicsStatistics();
        output.mainOutput = forumAccess.getForumsByIds(tids);
		return output;  
	}
}