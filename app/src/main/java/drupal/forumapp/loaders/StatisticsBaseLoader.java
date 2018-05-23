package drupal.forumapp.loaders;

        import android.content.AsyncTaskLoader;
        import android.content.Context;
        import android.text.TextUtils;

        import com.google.gson.Gson;

        import java.util.ArrayList;

        import drupal.forumapp.domain.SessionManager;
        import drupal.forumapp.model.ForumList;
        import drupal.forumapp.model.ForumModel;
        import drupal.forumapp.model.ForumTopicStatisticListModel;
        import drupal.forumapp.model.ForumTopicStatisticModel;
        import drupal.forumapp.serverAccess.ForumAccess;

/**
 * Created by serva on 10/17/2017.
 */

public abstract class StatisticsBaseLoader<T> extends BaseLoader<LoaderBaseResponse<T>> {

    public StatisticsBaseLoader(Context ctx) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
    }

    protected ForumTopicStatisticListModel getForumTopicsStatistics() {

        Gson gson = new Gson();
        SessionManager sessionManager = new SessionManager(getContext());
        ForumTopicStatisticListModel  currentStatistics = new ForumTopicStatisticListModel ();
        String currentStatisticsJson = sessionManager.getTopicsStatistics();
        if (!TextUtils.isEmpty(currentStatisticsJson)){
            currentStatistics = gson.fromJson(currentStatisticsJson, currentStatistics.getClass());
        }

        return currentStatistics;
    }
}
