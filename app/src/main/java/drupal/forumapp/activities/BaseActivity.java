package drupal.forumapp.activities;

import android.app.LoaderManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.Calendar;

import android.widget.Button;
import android.widget.ProgressBar;

import drupal.forumapp.R;
import drupal.forumapp.asyncTasks.DataListener;
import drupal.forumapp.asyncTasks.LoadTopicStatisticsAsyncTask;
import drupal.forumapp.asyncTasks.RefreshTokenAsyncTask;
import drupal.forumapp.asyncTasks.RefreshTokenTaskListener;
import drupal.forumapp.domain.GetStatisticsRunnable;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.domain.SessionManager;

import android.text.TextUtils;

import com.mikepenz.iconics.context.IconicsLayoutInflater;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.ForumTopicStatisticModel;

public abstract class BaseActivity extends AppCompatActivity {
    protected final int LOGIN_RESULT = 4;
    protected boolean mSlideState = false;
    protected DrawerLayout drawerLayout;
    protected AlertDialog deletePopUpWindow;
    protected AlertDialog navigateToLoginWindow;
    protected View popupLayout;
    protected LoaderManager loaderManager;

    private boolean isBackPressed = false;
    private boolean isInitialized = false;
    @Override
    public void finish() {
        super.finish();
        if (!isBackPressed) {
            Log.d("back", "activity finished");
            overridePendingTransitionExit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle bundle = intent.getExtras();

        if (!bundle.containsKey("key")) {
            return;
        }

        final SessionManager sessionManager = new SessionManager(this);
        final String username = sessionManager.getUsername();
        final View loginTextView = findViewById(R.id.loginButtonLayout);
        final View logoutButtonLayout = findViewById(R.id.logoutButtonLayout);
        AuthenticationResponseModel token = sessionManager.getAuthenticationToken();
        Boolean tokenExpired = (token == null || token.expirationDateTime.before(Calendar.getInstance().getTime()));
        if (!TextUtils.isEmpty(username) && tokenExpired == false) {
            // logined status
            loginTextView.setVisibility(View.GONE);
            logoutButtonLayout.setVisibility(View.VISIBLE);
        } else {
            // logined status
            loginTextView.setVisibility(View.VISIBLE);
            logoutButtonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        isBackPressed = false;
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Slide enterSlide = new Slide();
            enterSlide.setInterpolator(new LinearInterpolator());
            enterSlide.setSlideEdge(Gravity.LEFT);
            enterSlide.excludeTarget(android.R.id.statusBarBackground, true);
            enterSlide.setDuration(300);
            enterSlide.excludeTarget(android.R.id.navigationBarBackground, true);
            window.setExitTransition(enterSlide); // The Transition to use to move Views out of the scene when calling a new Activity.
            window.setReenterTransition(enterSlide); // The Transition to use to move Views into the scene when reentering from a previously-started Activity.
            // window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        } else {
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            Slide exitSlide = new Slide();
            exitSlide.setInterpolator(new LinearInterpolator());
            exitSlide.setSlideEdge(Gravity.RIGHT);
            exitSlide.setDuration(600);
            exitSlide.setStartDelay(300);
            exitSlide.excludeTarget(android.R.id.statusBarBackground, true);
            exitSlide.excludeTarget(android.R.id.navigationBarBackground, true);
            window.setReenterTransition(exitSlide); // The Transition to use to move Views into the initial Scene.
            window.setEnterTransition(exitSlide);
            window.setReturnTransition(exitSlide); // The Transition to use to move Views out of the Scene when the Window is preparing to close.
            //window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary, this.getTheme())));
        } else {
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }

    @Override
    public void onBackPressed() {
        this.getWindow().setAllowReturnTransitionOverlap(false);
        overridePendingTransitionExit();
        super.onBackPressed();
        isBackPressed = true;
        Log.d("back", "onBackPressed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        loaderManager = getLoaderManager();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isInitialized) {
                    initializationStep();
                }
            }
        },50);
    }

    protected void initializationStep(){
        try {
            isInitialized = true;
            ViewGroup drawerLayout = (ViewGroup) this.findViewById(R.id.drawerMenuContainer);
            View drawer = getLayoutInflater().inflate(R.layout.drawer_main_menu, drawerLayout, true);
            initializeDeleteConfirmationPopup();
            registerNewTopicsReceiver();

            LoadTopicStatisticsAsyncTask loadTopicStatisticsAsyncTask = new LoadTopicStatisticsAsyncTask(new SessionManager(this), new DataListener<ForumTopicStatisticListModel>() {
                public void onLoaded(ForumTopicStatisticListModel currentStatistics) {
                    if (currentStatistics == null || currentStatistics.size() == 0) {
                        return;
                    }

                    int newItems = 0;
                    for (int i = 0; i < currentStatistics.size(); i++) {
                        ForumTopicStatisticModel node = currentStatistics.get(i);
                        if (node.countNewNodes > 0) {
                            newItems += node.countNewNodes;
                        }
                    }

                    showNewItems(newItems);
                }
            });
            loadTopicStatisticsAsyncTask.execute();
        }
        catch (Exception ex){
            Log.e("initialization",ex.getMessage());
            Log.e("initialization", ex.getStackTrace().toString());
        }
    }

    protected void setupNewTopicsClickListener() {
        final Activity context = this;
        TextView newTopicsTextView = (TextView) findViewById(R.id.newTopicsTextView);
        if (newTopicsTextView != null) {
            newTopicsTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(context, NewTopicsActivity.class);
                    Bundle bundle = new Bundle();
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    navigateToLoginWindow.dismiss();
                }

                ;
            });
        }
    }

    protected void registerNewTopicsReceiver() {
        IntentFilter filter = new IntentFilter(GetStatisticsRunnable.INTENT_NAME);
        //you may want to set whatever filters here...
        //define the broadcast receiver
        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                receiveBroadcastMessage(intent);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    protected void receiveBroadcastMessage(Intent intent) {
        Bundle bundle = intent.getExtras();
        int newItems = bundle.getInt("NewTopics");
        showNewItems(newItems);
    }

    protected void showNewItems(int newItems) {
        TextView newTopicsTextView = (TextView) findViewById(R.id.newTopicsTextView);
        if (newTopicsTextView != null) {
            newItems = newItems > 99 ? 99 : newItems;
            if (newItems > 0) {
                newTopicsTextView.setVisibility(View.VISIBLE);
                newTopicsTextView.setText("" + newItems);
            } else {
                newTopicsTextView.setVisibility(View.GONE);
            }
        }
    }

    protected void showDeleteDialog() {
        showDeleteDialog("");
    }

    protected void cancelDeleteOperation() {

    }

    protected void showDeleteDialog(String message) {
        if (!TextUtils.isEmpty(message)) {
            TextView textView = (TextView) popupLayout.findViewById(R.id.descriptionTextView);
            textView.setText(message);
        }

        deletePopUpWindow.show();
        ProgressBar deleteProgressBar = (ProgressBar) popupLayout.findViewById(R.id.deleteProgressBar);
        deleteProgressBar.setVisibility(View.GONE);
    }

    protected void initializeNavigateToLoginPopup() {

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupLayout = getLayoutInflater().inflate(R.layout.dialog_navigate_to_login, null, false);
        builder.setView(popupLayout);
        navigateToLoginWindow = builder.create();
        navigateToLoginWindow.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // navigate To Login Button
        final Activity context = this;
        Button navigateToLoginButton = (Button) popupLayout.findViewById(R.id.navigateToLoginButton);
        navigateToLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                context.startActivityForResult(intent, LOGIN_RESULT);
                navigateToLoginWindow.dismiss();
            }

            ;
        });
    }

    protected void initializeDeleteConfirmationPopup() {

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        popupLayout = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        builder.setView(popupLayout);
        deletePopUpWindow = builder.create();
        deletePopUpWindow.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final ProgressBar deleteProgressBar = (ProgressBar) popupLayout.findViewById(R.id.deleteProgressBar);

        // no Button
        Button deleteNoButton = (Button) popupLayout.findViewById(R.id.NoButton);
        deleteNoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deletePopUpWindow.dismiss();
                cancelDeleteOperation();
            }
        });

        // yes Button
        Button deleteYesButton = (Button) popupLayout.findViewById(R.id.YesButton);
        deleteYesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteProgressBar.setVisibility(View.VISIBLE);
                deleteItems();
            }
        });

    }

    protected void deleteItems() {

    }

    protected final void hideDeleteDialog() {
        deletePopUpWindow.dismiss();
    }

    protected void drawerInitialization() {

        //drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        TextView drawerIcon = (TextView) findViewById(R.id.drawerIcon);
        if (drawerIcon != null) {
            drawerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSlideState) {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
            });
        }

        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mSlideState = false;//is Closed
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mSlideState = true;//is Opened
            }
        });
    }

    protected void bindDrawerButtons() {

        final Activity context = this;
        final SessionManager sessionManager = new SessionManager(this);
        final String username = sessionManager.getUsername();

        AuthenticationResponseModel token = sessionManager.getAuthenticationToken();
        Boolean tokenExpired = (token == null || token.expirationDateTime.before(Calendar.getInstance().getTime()));

        // login
        final View welcomeLayout = findViewById(R.id.welcomeTextLayout);
        final TextView welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        final View loginTextView = findViewById(R.id.loginButtonLayout);
        final View logoutButtonLayout = findViewById(R.id.logoutButtonLayout);

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                drawerLayout.closeDrawer(Gravity.LEFT);
                context.startActivityForResult(intent, LOGIN_RESULT);
            }
        });

        logoutButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.clearAuthentications();
                logoutButtonLayout.setVisibility(View.GONE);
                loginTextView.setVisibility(View.VISIBLE);
                welcomeTextView.setText("");
            }
        });

        if (!TextUtils.isEmpty(username) && tokenExpired == false) {
            // logined status
            loginTextView.setVisibility(View.GONE);
            logoutButtonLayout.setVisibility(View.VISIBLE);
        } else {
            if (!TextUtils.isEmpty(username)) {

                // logined but expired
                RefreshTokenAsyncTask refreshTokenAsyncTask = new RefreshTokenAsyncTask(sessionManager, new RefreshTokenTaskListener() {
                    @Override
                    public void onTokenRefreshed(AuthenticationResponseModel token) {
                        // logout
                        loginTextView.setVisibility(View.GONE);
                        logoutButtonLayout.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(username)) {

                            // welcome layout
                            welcomeLayout.setVisibility(View.VISIBLE);

                            welcomeTextView.setText("Welcome " + username);
                        }
                    }
                });
                refreshTokenAsyncTask.execute(token);

                // welcome layout
                welcomeLayout.setVisibility(View.VISIBLE);

                welcomeTextView.setText("Welcome " + username);
            }

            loginTextView.setVisibility(View.VISIBLE);
            logoutButtonLayout.setVisibility(View.GONE);
        }

        // startPage
        View startPageButtonLayout = findViewById(R.id.startPageButtonLayout);
        startPageButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(context, ForumsActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        // favorites
        View favoritesButtonLayout = findViewById(R.id.favoritesButtonLayout);
        favoritesButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(context, FavoritesActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        // settings
        View settingsButtonLayout = findViewById(R.id.settingsButtonLayout);
        settingsButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(context, MainSettingsActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        // information
        View informationButtonLayout = findViewById(R.id.informationButtonLayout);
        informationButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                Intent intent = new Intent(context, InfoActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }
}
