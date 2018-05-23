package drupal.forumapp.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import drupal.forumapp.R;
import drupal.forumapp.activities.EditCommentActivity;
import drupal.forumapp.activities.EditTopicActivity;
import drupal.forumapp.activities.TopicCommentsActivity;
import drupal.forumapp.asyncTasks.SetTopicAsVisitedAsyncTask;
import drupal.forumapp.domain.DateHelper;
import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.model.FavoriteTopicModel;
import drupal.forumapp.model.ForumTopicStatisticModel;
import drupal.forumapp.model.TopicModel;

import android.support.annotation.LayoutRes;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.AddRemoveFavoriteTopicAsyncTask;
import drupal.forumapp.asyncTasks.GenericListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.UserModel;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.asyncTasks.DataListener;

import android.widget.PopupMenu;

import android.text.TextUtils;

public class ForumTopicsAdapter extends CustomBaseAdapter<TopicModel> {
    private LoadFileAsyncTask loadFileAsyncTask;
    private List<FavoriteTopicModel> favorites;
    private ForumModel forumModel;
    private ArrayList<ForumTopicStatisticModel> forumTopicsStatistics;
    private View listView;
    private TopicModel clickedTopic;
    protected AlertDialog popUpWindow;
    protected HashSet<Integer> visitedNodes = new HashSet<Integer>();
    private DataListener<TopicModel> deleteClickHandler;

    public ForumTopicsAdapter(final Activity context, @LayoutRes int id, ArrayList<TopicModel> data, LoadFileAsyncTask loadFileAsyncTask, ForumModel forumModelData, ArrayList<ForumTopicStatisticModel> forumTopicsStatistics, View listview, DataListener<TopicModel> deleteClickHandlerP) {
        super(context, id, data);
        this.deleteClickHandler = deleteClickHandlerP;
        this.loadFileAsyncTask = loadFileAsyncTask;
        this.forumModel = forumModelData;
        this.listView = listview;
        this.forumTopicsStatistics = forumTopicsStatistics;
        this.touchableAnimationRes = R.drawable.layout_list_item_animation;
        this.touchableOriginalRes = R.drawable.layout_forum_list_item;
        loadFavorites();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        View popupLayout = context.getLayoutInflater().inflate(R.layout.dialog_topic_menu, null, false);
        builder.setView(popupLayout);
        popUpWindow = builder.create();
        popUpWindow.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView addCommentMenuTextView = (TextView) popupLayout.findViewById(R.id.addCommentMenuTextView);
        TextView deleteMenuTextView = (TextView) popupLayout.findViewById(R.id.deleteMenuTextView);
        TextView editMenuTextView = (TextView) popupLayout.findViewById(R.id.editMenuTextView);
        TextView viewMenuTextView = (TextView) popupLayout.findViewById(R.id.viewMenuTextView);

        viewMenuTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TopicModel data = clickedTopic;
                if (data == null) {
                    return;
                }

                Intent intent = new Intent(context, TopicCommentsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("topicId", data.nid);
                Gson gson = new Gson();
                bundle.putString("topic", gson.toJson(data));
                bundle.putString("forum", gson.toJson(forumModel));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        addCommentMenuTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TopicModel data = clickedTopic;
                if (data == null) {
                    return;
                }

                Intent intent = new Intent(context, EditCommentActivity.class);
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                bundle.putString("topic", gson.toJson(data));
                bundle.putString("forum", gson.toJson(forumModel));
                bundle.putBoolean("InAddMode", true);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        editMenuTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TopicModel data = clickedTopic;
                if (data == null) {
                    return;
                }

                Intent intent = new Intent(context, EditTopicActivity.class);
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                bundle.putString("forum", gson.toJson(forumModel));
                bundle.putString("topic", gson.toJson(data));
                bundle.putBoolean("InAddMode", false);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        if (deleteClickHandler != null) {
            deleteMenuTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (clickedTopic == null) {
                        return;
                    }

                    popUpWindow.hide();
                    deleteClickHandler.onLoaded(clickedTopic);
                }
            });
        }

        //addCommentMenuTextView
        // deleteMenuTextView
        // editMenuTextView
        //viewMenuTextView
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        loadFavorites();
    }

    public void setLoadFileAsyncTask(LoadFileAsyncTask loadFileAsyncTask){
        this.loadFileAsyncTask = loadFileAsyncTask;
    }

    @Override
    protected TopicModel[] createArray(int size) {
        return new TopicModel[size];
    }

    protected ForumTopicStatisticModel getForumStatistics(int tid) {
        if (forumTopicsStatistics != null) {
            for (ForumTopicStatisticModel forumTopicStatisticModel : forumTopicsStatistics) {
                if (forumTopicStatisticModel.tid == tid) {
                    return forumTopicStatisticModel;
                }
            }
        }

        return null;
    }

    protected void loadFavorites() {
        Log.i("favorites", "start loading");
        SessionManager sessionManager = new SessionManager(context);
        String favoritesJson = sessionManager.getFavoriteTopics();
        favorites = new ArrayList<FavoriteTopicModel>();
        if (!TextUtils.isEmpty(favoritesJson)) {
            Moshi moshi = new Moshi.Builder().build();
            Type type = Types.newParameterizedType(List.class, FavoriteTopicModel.class);
            JsonAdapter<List<FavoriteTopicModel>> adapter = moshi.adapter(type);

            try {
                favorites = adapter.fromJson(favoritesJson);
                Log.i("favorites", "end loading");
            } catch (IOException e) {
                Log.e("loadFavorites", e.getMessage());
                Log.e("loadFavorites", e.getStackTrace().toString());
            }
        }
    }

    protected void bindData(int position, View view, final TopicModel data) {
        View topicItemBodyLayout = view.findViewById(R.id.topicItemBodyLayout);
        topicItemBodyLayout.setTag(R.id.topic_title, data);
        View lockIconTextView = view.findViewById(R.id.lockIconTextView);
        //lockItemsBackground
        if (data.comment == TopicModel.COMMENTS_CLOSED) {
            lockIconTextView.setVisibility(View.VISIBLE);
            // topicItemBodyLayout.setBackgroundResource(R.drawable.layout_forum_list_locked_item);
        } else {
            lockIconTextView.setVisibility(View.GONE);
            //   topicItemBodyLayout.setBackgroundResource(R.drawable.layout_forum_list_item);
        }

        TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
        titleView.setTag(R.id.topic_title, data);
        titleView.setText(data.title);

        final View userIconTextView = view.findViewById(R.id.userIconTextView);
        final ImageView userImageView = (ImageView) view.findViewById(R.id.userImageView);
        if(!TextUtils.isEmpty(data.localUserImagePath)){
            Bitmap bitmap = BitmapFactory.decodeFile(data.localUserImagePath);
            userImageView.setImageBitmap(bitmap);
            userIconTextView.setVisibility(View.GONE);
            userImageView.setVisibility(View.VISIBLE);
        }
        else{
            userIconTextView.setVisibility(View.VISIBLE);
            userImageView.setVisibility(View.GONE);
        }

        loadFileAsyncTask.setListener(new LoadFileTaskListener() {
            public void onFileAvailable(FileInfo file) {
                if (file.uid == data.uuid) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.localPath);
                    userImageView.setImageBitmap(bitmap);
                    userIconTextView.setVisibility(View.GONE);
                    userImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        View userIconLayout = view.findViewById(R.id.userLayout);
        UserModel userModel = new UserModel();
        userModel.uid = data.uuid;
        userModel.name = data.name;
        userModel.picture = data.picture;
        userIconLayout.setTag(R.id.user_icon_data, userModel);

        TextView bodyView = (TextView) view.findViewById(R.id.bodyTextView);
        bodyView.setText(HtmlHelper.fromHtml(data.body));

        TextView usernameView = (TextView) view.findViewById(R.id.usernameTextView);
        usernameView.setText(data.name);

        TextView createdDateView = (TextView) view.findViewById(R.id.createdDateTextView);
        createdDateView.setText(DateHelper.format(data.created));

        TextView commentsCountTextView = (TextView) view.findViewById(R.id.commentsCountTextView);
        commentsCountTextView.setText(data.comment_count + " " + context.getResources().getString(R.string.topic_comments_count));

        final View selctedLayout = view.findViewById(R.id.selctedLayout);
        selctedLayout.setTag(R.id.current_item, data);
        if (selectedItems.contains(position)) {
            if (selectedItemsHeight.containsKey(position)) {
                selctedLayout.getLayoutParams().height = selectedItemsHeight.get(position);
            }
            selctedLayout.setVisibility(View.VISIBLE);

        } else {
            selctedLayout.setVisibility(View.GONE);
        }

        final TextView favoriteTextView = (TextView) view.findViewById(R.id.favoriteIconTextView);
        setFavoriteTextViewColor(favoriteTextView, data.nid);

        final ForumTopicStatisticModel forumTopicStatisticModel = getForumStatistics(data.forum_tid);
        TextView newItemSignTextView = (TextView) view.findViewById(R.id.newItemSignTextView);
        if (newItemSignTextView != null) {
            if (forumTopicStatisticModel != null &&
                    forumTopicStatisticModel.countNewNodes > 0 &&
                    forumTopicStatisticModel.maxVisitedNid < data.nid &&
                    !visitedNodes.contains(data.nid)) {
                newItemSignTextView.setVisibility(View.VISIBLE);
                visitedNodes.add(data.nid);
                SetTopicAsVisitedAsyncTask setTopicAsVisitedAsyncTask = new SetTopicAsVisitedAsyncTask(new SessionManager(this.context), new GenericListener() {
                    @Override
                    public void onFinished() {
                        forumTopicStatisticModel.maxVisitedNid = forumTopicStatisticModel.maxVisitedNid > data.nid ?
                                forumTopicStatisticModel.maxVisitedNid : data.nid;
                    }
                });
                setTopicAsVisitedAsyncTask.execute(data);
            } else {
                newItemSignTextView.setVisibility(View.GONE);
            }
        }
    }

    protected void setFavoriteTextViewColor(TextView favoriteTextView, int nid) {
        FavoriteTopicModel found = FavoriteTopicModel.find(favorites, nid);
        if (found != null) {
            favoriteTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        } else {
            favoriteTextView.setTextColor(context.getResources().getColor(R.color.unSelectedText, context.getTheme()));
        }
    }

    protected void bindEvents(final int position, View view, final TopicModel topicModel) {
        final TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
        titleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TopicModel data = (TopicModel) view.getTag(R.id.topic_title);
                Intent intent = new Intent(context, TopicCommentsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("topicId", data.nid);
                Gson gson = new Gson();
                bundle.putString("topic", gson.toJson(data));
                bundle.putString("forum", gson.toJson(forumModel));
                intent.putExtras(bundle);
                context.startActivity(intent);

            }
        });

        final View selctedLayout = view.findViewById(R.id.selctedLayout);
        final View mainView = view;
        final View userIconLayout = view.findViewById(R.id.userLayout);
        View topicItemBodyLayout = view.findViewById(R.id.topicItemBodyLayout);
        topicItemBodyLayout.setOnTouchListener(createTimerTouchListener(topicItemBodyLayout, selctedLayout, mainView));
        userIconLayout.setOnClickListener(new UserClickListener(context));

        selctedLayout.setOnTouchListener(createItemLayoutTouchListener(selctedLayout));

        final TextView favoriteTextView = (TextView) view.findViewById(R.id.favoriteIconTextView);
        final ProgressBar topicFavoriteProgressBar = (ProgressBar)view.findViewById(R.id.topicFavoriteProgressBar);
        final SessionManager sessionManager = new SessionManager(context);
        favoriteTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final TopicModel topicModel = (TopicModel) titleView.getTag(R.id.topic_title);
                FavoriteTopicModel found = FavoriteTopicModel.find(favorites, topicModel.nid);
                final Boolean addMode = found == null;

                AddRemoveFavoriteTopicAsyncTask addRemoveFavoriteTopicAsyncTask = new AddRemoveFavoriteTopicAsyncTask(sessionManager, addMode, new GenericListener() {

                    public void onFinished() {
                        loadFavorites();
                        setFavoriteTextViewColor(favoriteTextView, topicModel.nid);
                        topicFavoriteProgressBar.setVisibility(View.GONE);
                        favoriteTextView.setVisibility(View.VISIBLE);
                    }
                });

                favoriteTextView.setVisibility(View.GONE);
                topicFavoriteProgressBar.setVisibility(View.VISIBLE);
                addRemoveFavoriteTopicAsyncTask.execute(topicModel);
            }
        });
    }

    @Override
    protected void showMenu(View view) {
        SessionManager sessionManager = new SessionManager(this.context);
        final String username = sessionManager.getUsername();
        if (!TextUtils.isEmpty(username)) {
            clickedTopic = (TopicModel) view.getTag(R.id.topic_title);
            popUpWindow.show();
        }
    }
}