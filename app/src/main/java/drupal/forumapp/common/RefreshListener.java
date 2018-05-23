package drupal.forumapp.common;

import android.support.v4.widget.SwipeRefreshLayout;
import android.content.Loader;

import java.util.ArrayList;

import drupal.forumapp.loaders.BaseLoader;

public class RefreshListener implements SwipeRefreshLayout.OnRefreshListener {

    private BaseLoader<?> loader;
    private ArrayList<?> dataList;

    public RefreshListener(BaseLoader<?> loader, ArrayList<?> dataList) {
        this.loader = loader;
        this.dataList = dataList;
    }

    @Override
    public void onRefresh() {
        dataList.clear();
        loader.setNextPage(0);
        loader.forceLoad();
    }
}