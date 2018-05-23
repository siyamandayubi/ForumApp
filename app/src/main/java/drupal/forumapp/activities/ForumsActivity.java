package drupal.forumapp.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import drupal.forumapp.adapters.ForumAdapter;

import drupal.forumapp.R;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.domain.AuthenticationManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.loaders.ForumsLoader;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.activities.LoginActivity;
import drupal.forumapp.activities.ServerSettingActivity;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.common.RefreshListener;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.loaders.LoaderBaseResponse;

import android.text.TextUtils;

public class ForumsActivity extends ListBaseActivity<ForumModel> implements LoaderManager.LoaderCallbacks {
    protected final int LOADER_ID = 100;
    private ListView listView;
    private ForumAdapter adapter;
    private boolean mSlideState = false;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forums);
    }


    @Override
    protected void initializationStep() {
        super.initializationStep();
        initialize();
        setupNewTopicsClickListener();
    }

	protected void initialize() {
	
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerInitialization();

        bindDrawerButtons();

        listInitialization();

        // loader
        loader = (BaseLoader<?>) loaderManager.initLoader(LOADER_ID, null, this);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(loader, dataList));
	}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new ForumsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
		LoaderBaseResponse<ForumList> loaderResponse = (LoaderBaseResponse<ForumList>) data;
        ForumList forumList = (ForumList) loaderResponse.mainOutput;
        listView = (ListView) findViewById(R.id.listView);
        View progressBar = findViewById(R.id.progressBarLayout);
        progressBar.setVisibility(View.GONE);
        View listViewLayout = findViewById(R.id.listViewLayout);
        listViewLayout.setVisibility(View.VISIBLE);
        try {
            dataList.addAll(forumList.forums.values());

            View noItemExists = findViewById(R.id.noItemLayout);
            if (dataList.size() > 0) {
				listView.setVisibility(View.VISIBLE);
                noItemExists.setVisibility(View.GONE);
            } else {
				listView.setVisibility(View.GONE);
                noItemExists.setVisibility(View.VISIBLE);
            }

            final Context context = this;
            if (adapter == null) {
                adapter = new ForumAdapter(this, R.layout.forum_list_item, dataList, loaderResponse.forumTopicsStatistics);
                adapter.setDataSelectedListener(new DataSelectedListener<ForumModel>() {
                    public void onSelect(Integer[] indexes, ForumModel[] selectedData) {
                        refreshMenus(indexes, selectedData, context);
                        TextView selectedItemsTextView = (TextView) findViewById(R.id.selectedItemsTextView);
                        selectedItemsTextView.setText(selectedItems.length + " Selected");
                    }
                });
                listView.setAdapter(adapter);
            } else {
                adapter.setForumTopicsStatistics(loaderResponse.forumTopicsStatistics);
                adapter.notifyDataSetChanged();
            }

            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception ex) {
            Log.e("onLoadFinished", ex.getMessage());
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        loader.reset();
    }

    @Override
    protected Class<ForumModel> getType() {
        ForumModel forumModel = new ForumModel();
        return (Class<ForumModel>) forumModel.getClass();
    }

    @Override
    protected Intent getEditIntent() {
        Intent intent = new Intent(this, EditForumActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("forum", gson.toJson(selectedItems[0]));
        bundle.putBoolean("InAddMode", false);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected Intent getAddIntent() {
        Intent intent = new Intent(this, EditForumActivity.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putBoolean("InAddMode", true);
        intent.putExtras(bundle);
        return intent;
    }

    private class AddButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public AddButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EditForumActivity.class);
            context.startActivity(intent);
        }
    }
}
