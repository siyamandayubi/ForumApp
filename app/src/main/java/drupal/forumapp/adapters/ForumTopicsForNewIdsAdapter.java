package drupal.forumapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

import drupal.forumapp.R;
import drupal.forumapp.activities.TopicCommentsActivity;
import drupal.forumapp.domain.DateHelper;
import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.model.FavoriteTopicModel;
import drupal.forumapp.model.ForumTopicStatisticModel;
import drupal.forumapp.model.TopicModel;

import android.support.annotation.LayoutRes;
import android.widget.TextView;

import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.asyncTasks.AddRemoveFavoriteTopicAsyncTask;
import drupal.forumapp.asyncTasks.GenericListener;
import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.UserModel;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.ForumList;

import android.text.TextUtils;

public class ForumTopicsForNewIdsAdapter extends ForumTopicsAdapter {

    private ForumList forumList;

    public ForumTopicsForNewIdsAdapter(Activity context,
                                       @LayoutRes int id,
                                       ArrayList<TopicModel> data,
                                       LoadFileAsyncTask loadFileAsyncTask,
                                       ForumList forumList,
                                       ArrayList<ForumTopicStatisticModel> forumTopicsStatistics,
                                       View view) {
        super(context, id, data, loadFileAsyncTask, null, forumTopicsStatistics, view, null);
        this.forumList = forumList;
    }

    protected void bindData(int position, View view, TopicModel data) {
        super.bindData(position, view, data);

        if (this.forumList != null) {
            for (ForumModel forum : this.forumList.forums.values()) {
                if (forum.tid == data.forum_tid) {
                    TextView forumTextView = (TextView) view.findViewById(R.id.forumTitleInTopicTextView);
                    forumTextView.setText(forum.name);
                }
            }
        }
    }
}
