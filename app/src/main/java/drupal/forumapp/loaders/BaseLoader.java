package drupal.forumapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.serverAccess.ForumAccess;

/**
 * Created by serva on 10/17/2017.
 */

public abstract class BaseLoader<T> extends AsyncTaskLoader<T> {
    // We hold a reference to the Loader’s data here.
    public static final int PAGE_SIZE = 10;
    private T mData;
    private int page;
    protected int totalCount = 0;
	protected boolean refreshData = false;

    public BaseLoader(Context ctx) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(ctx);
        page = 0;
    }

    public void setNextPage(int page) {
 		this.refreshData = true;
       this.page = page;
    }

    public void clearCache(){
        this.refreshData = true;
    }

    public int getNextPage() {
        return page;
    }

    @Override
    public void forceLoad(){
		this.refreshData = true;
        super.forceLoad();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getMaxPage() {
        int max = totalCount / PAGE_SIZE;
        int rem = totalCount % PAGE_SIZE;
        if (rem > 0) {
            max++;
        }

        return max - 1;
    }

    @Override
    public void reset() {
        setNextPage(0);
        super.reset();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mData != null) {
            deliverResult(mData);
        }

        if (mData == null || takeContentChanged()) {
            super.forceLoad();
        }
    }

    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        T oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    private void releaseResources(T data) {
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
    }
}
