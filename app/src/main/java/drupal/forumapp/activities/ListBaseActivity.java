package drupal.forumapp.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.util.ArrayList;
import java.util.Calendar;

import drupal.forumapp.R;
import drupal.forumapp.adapters.CustomBaseAdapter;
import drupal.forumapp.asyncTasks.RefreshTokenAsyncTask;
import drupal.forumapp.asyncTasks.RefreshTokenTaskListener;
import drupal.forumapp.common.AnimationHelper;
import drupal.forumapp.common.AppBarStateChangeListener;
import drupal.forumapp.common.EndlessScrollListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.loaders.BaseLoader;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.model.ShareParcelable;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.common.ShareHelper;
import drupal.forumapp.asyncTasks.LockTopicsAsyncTask;

public abstract class ListBaseActivity<T extends Object> extends BaseActivity {
    protected static final int ADD_REQUEST = 1;
    protected static final int EDIT_REQUEST = 2;
    protected View selectMenu;
    protected View normalMenu;
    protected T[] selectedItems;
    protected Integer[] selectedIndexes;
    protected View editButton;
    protected View deleteButton;
    protected View selectMenuLockButton;
    protected View selectMenuUnLockButton;
    protected CustomBaseAdapter<T> adapter;
    protected ArrayList<T> dataList;
    protected ProgressBar loadMoreProgressBar;
    protected ListView listView;
    protected BaseLoader<?> loader;
    protected Boolean addNewItemsToBeginOfTheList;
	protected View addButton;

	public T[] getSelectedItems(){
	    return selectedItems;
    }

    protected void listInitialization() {

        addNewItemsToBeginOfTheList = true;

        initializeNavigateToLoginPopup();

        editButton = findViewById(R.id.editButton);
        if (editButton != null) {
            editButton.setOnClickListener(new EditButtonClickListener(this));
        }

        selectMenu = findViewById(R.id.selectedToolbarMenu);
        normalMenu = findViewById(R.id.normalToolbarMenu);
        loadMoreProgressBar = (ProgressBar) findViewById(R.id.loadMoreProgressBar);

        addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            addButton.setOnClickListener(new AddButtonClickListener(this));
        }

        dataList = new ArrayList<T>();

        if (selectMenu != null) {
            deleteButton = findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new DeleteButtonClickListener(this));

            selectMenuLockButton = findViewById(R.id.lockButton);
            selectMenuUnLockButton = findViewById(R.id.unlockButton);
            if (selectMenuLockButton != null) {
                selectMenuLockButton.setOnClickListener(new LockUnLockClickListener(this, true));
            }

            if (selectMenuUnLockButton != null) {
                selectMenuUnLockButton.setOnClickListener(new LockUnLockClickListener(this, false));
            }

            View shareButton = findViewById(R.id.shareButton);
            shareButton.setOnClickListener(new ShareButtonClickListener(this));

            View closeSelectedMenuTextView = findViewById(R.id.closeSelectedMenuTextView);
            final Context context = this;
            closeSelectedMenuTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshMenus(new Integer[0], null, context);
                    adapter.clearSelection();
                }
            });
        }

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnScrollListener(new EndlessScrollListener(0) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (loader.getMaxPage() >= loader.getNextPage()) {
                    loader.forceLoad();
                    loadMoreProgressBar.setVisibility(View.VISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(resultCode, resultCode, intent);
        // Check which request we're responding to
        if (resultCode != RESULT_OK) {
            return;
        }

        if (adapter == null){
            return;
        }

        View noItemExists = findViewById(R.id.noItemLayout);
        noItemExists.setVisibility(View.GONE);

        Bundle bundle = intent.getExtras();
        String dataJson = bundle.getString("data");
        Gson gson = new Gson();
        T data = gson.fromJson(dataJson, getType());
        if (requestCode == ADD_REQUEST) {
            if (addNewItemsToBeginOfTheList) {
                dataList.add(0, data);
            } else {
                dataList.add(dataList.size() - 1, data);
            }
        } else if (requestCode == EDIT_REQUEST) {
            dataList.set(selectedIndexes[0], data);
        }

        refreshMenus(new Integer[0], null, this);
        adapter.clearSelection();
        adapter.notifyDataSetChanged();
    }

    protected void refreshMenus(Integer[] indexes, T[] selectedData, Context context) {
        if (selectedData != null && selectedData.length > 0 &&
                (selectedItems == null || selectedItems.length == 0)) {
            AnimationHelper.expandToolbar(context, selectMenu);
            normalMenu.setVisibility(View.GONE);
        }

        if (selectedData == null || selectedData.length == 0) {
            AnimationHelper.collapseToolbar(context, selectMenu, normalMenu);
        }

        if (selectedData != null && selectedData.length > 1) {
            editButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
        }

        if (showSelectMenuLockIcon(selectedData)) {
            selectMenuLockButton.setVisibility(View.VISIBLE);
        } else {
            selectMenuLockButton.setVisibility(View.GONE);
        }

        if (showSelectMenuUnLockIcon(selectedData)) {
            selectMenuUnLockButton.setVisibility(View.VISIBLE);
        } else {
            selectMenuUnLockButton.setVisibility(View.GONE);
        }

        selectedItems = selectedData;
        selectedIndexes = indexes;
    }

    protected Boolean showSelectMenuLockIcon(T[] selectedData) {
        return false;
    }

    protected Boolean showSelectMenuUnLockIcon(T[] selectedData) {
        return false;
    }

    protected abstract Class<T> getType();

    protected abstract Intent getEditIntent();

    protected abstract Intent getAddIntent();

    protected void lockUnLockSelectedItems(Boolean lock) {
    }

    protected class ShareButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public ShareButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            if (selectedItems != null && selectedItems.length > 0) {
                String content = "";
                for (T item : selectedItems) {
                    final String itemValue = ShareHelper.getToSharedString(item);
                    content = content + String.format("<li>%1$s</li>", itemValue);
                    ShareParcelable itemToShare = new ShareParcelable(itemValue);
                }

                content = String.format("<ul>%1$s</ul>", content);

//                Intent x = ShareCompat
//                        .IntentBuilder
//                        .from(this.context)
//                        .setType("text/html")
//                        .setHtmlText(content)
//                        .getIntent();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);

                try {
                    sendIntent.setType("text/html");
                    startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.sharing_window_title)));
                } catch (Exception ex) {
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
                //x.setAction(Intent.ACTION_SEND);
                //startActivity(x);
            }
        }
    }

    protected class DeleteButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public DeleteButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (selectedItems != null && selectedItems.length > 0) {
                SessionManager sessionManager = new SessionManager(context);
                final String username = sessionManager.getUsername();

                if (TextUtils.isEmpty(username)) {
                    navigateToLoginWindow.show();
                } else {
                    showDeleteDialog();
                }
            }
        }
    }

    protected class AddButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public AddButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            try {
                SessionManager sessionManager = new SessionManager(context);
                final String username = sessionManager.getUsername();

                if (TextUtils.isEmpty(username)) {
                    navigateToLoginWindow.show();
                } else {
                    Intent intent = getAddIntent();
                    context.startActivityForResult(intent, ADD_REQUEST);
                }
            } catch (Exception ex) {
                Log.e("AddTopicButton", ex.getMessage());
                Log.e("AddTopicButton", ex.getStackTrace().toString());
            }
        }
    }

    protected class ScrollActivityInitializer {
        private final Toolbar toolbar;
        private Activity context;
        private CollapsingToolbarLayout collapsingToolbarLayout;
        private ListView listView;

        public ScrollActivityInitializer(Activity context, Toolbar toolbar, ListView listview) {
            this.toolbar = toolbar;
            this.listView = listview;
            this.context = context;
        }

        public Activity getContext() {
            return context;
        }

        public CollapsingToolbarLayout getCollapsingToolbarLayout() {
            return collapsingToolbarLayout;
        }

        public ScrollActivityInitializer invoke() {
            listView.setNestedScrollingEnabled(true);
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
            final View normalToolbarMenu = findViewById(R.id.normalToolbarMenu);
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        toolbar.setVisibility(View.VISIBLE);
                        AnimationHelper.expandToolbar(context, normalToolbarMenu, 500);
                    } else {
                        toolbar.setVisibility(View.INVISIBLE);
                        normalToolbarMenu.setVisibility(View.GONE);
                    }
                }
            });

            collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            collapsingToolbarLayout.setTitle("");
            //collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent, this.getTheme()));
            //collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white, this.getTheme()));
            //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
            return this;
        }
    }

    protected class EditButtonClickListener implements View.OnClickListener {

        private BaseActivity context;

        public EditButtonClickListener(BaseActivity context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            if (selectedItems != null && selectedItems.length > 0) {
                SessionManager sessionManager = new SessionManager(context);
                final String username = sessionManager.getUsername();

                if (TextUtils.isEmpty(username)) {
                    navigateToLoginWindow.show();
                } else {
                    Intent intent = getEditIntent();
                    context.startActivityForResult(intent, EDIT_REQUEST);
                }
            }
        }
    }

    protected class LockUnLockClickListener implements View.OnClickListener {

        private BaseActivity context;
        private Boolean lockUnLock;

        public LockUnLockClickListener(BaseActivity context, Boolean lockUnLock) {
            this.context = context;
            this.lockUnLock = lockUnLock;
        }

        @Override
        public void onClick(View v) {

            lockUnLockSelectedItems(lockUnLock);
        }
    }

}
