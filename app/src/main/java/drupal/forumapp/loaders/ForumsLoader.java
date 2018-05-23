package drupal.forumapp.loaders;

        import android.content.AsyncTaskLoader;
        import android.content.Context;

        import java.util.ArrayList;

        import drupal.forumapp.domain.SessionManager;
        import drupal.forumapp.model.ForumList;
        import drupal.forumapp.model.ForumModel;
        import drupal.forumapp.serverAccess.ForumAccess;
		import drupal.forumapp.App;
/**
 * Created by serva on 10/17/2017.
 */

public class ForumsLoader extends StatisticsBaseLoader<ForumList> {
	private final static String CACHE_ID = "FORUMS_LOADER";
    public ForumsLoader(Context ctx) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
    }

    @Override
    public LoaderBaseResponse<ForumList> loadInBackground() {
        App app = (App)this.getContext().getApplicationContext();
		if(!refreshData){
            LoaderBaseResponse<ForumList> data = (LoaderBaseResponse<ForumList>)app.getCacheItem(CACHE_ID);
			if (data != null){
                data.forumTopicsStatistics = getForumTopicsStatistics();
				return  data;
			}
		}

		LoaderBaseResponse<ForumList> output = new LoaderBaseResponse<ForumList>();
        ForumAccess forumAccess = new ForumAccess(new SessionManager(this.getContext()));
        output.mainOutput = forumAccess.getForums();
		output.forumTopicsStatistics = getForumTopicsStatistics();
		app.putCacheItem(CACHE_ID, output);
		return output;
    }
}
