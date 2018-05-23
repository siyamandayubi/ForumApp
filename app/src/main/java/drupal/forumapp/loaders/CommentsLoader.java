package drupal.forumapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.serverAccess.CommentsAccess;
import drupal.forumapp.serverAccess.TopicsAccess;
import drupal.forumapp.model.ListModel;
import drupal.forumapp.model.CommentModel;
/**
 * Created by serva on 10/17/2017.
 */

public class CommentsLoader extends BaseLoader<ListModel<CommentModel>> {
    private int topicId;
    public CommentsLoader(Context ctx, int topicId) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
        this.topicId = topicId;
    }
    @Override
    public ListModel<CommentModel> loadInBackground() {
        CommentsAccess commentsAccess = new CommentsAccess(new SessionManager(this.getContext()));
        int page = getNextPage();
        ListModel<CommentModel> comments = commentsAccess.getComments(topicId, page);
        totalCount = comments.count;
        
        setNextPage(page + 1);

        return comments;
    }
}
