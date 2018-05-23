package drupal.forumapp.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import drupal.forumapp.asyncTasks.FileInfo;
import drupal.forumapp.domain.DateHelper;

import java.util.ArrayList;
import java.util.Collection;

import android.text.TextUtils;

import drupal.forumapp.R;
import drupal.forumapp.loaders.ForumsLoader;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.CommentModel;

import android.support.annotation.LayoutRes;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import drupal.forumapp.activities.EditCommentActivity;
import drupal.forumapp.domain.HtmlHelper;
import drupal.forumapp.asyncTasks.LoadFileTaskListener;
import drupal.forumapp.asyncTasks.LoadFileAsyncTask;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.UserModel;

public class CommentsAdapter extends CustomBaseAdapter<CommentModel> {
    private LoadFileAsyncTask loadFileAsyncTask;
    private TopicModel topicModel;

    public CommentsAdapter(Context context, @LayoutRes int id, ArrayList<CommentModel> data, LoadFileAsyncTask loadFileAsyncTask, TopicModel topicModel) {
        super(context, id, data);
        this.topicModel = topicModel;
        this.loadFileAsyncTask = loadFileAsyncTask;
    }

    @Override
    protected CommentModel[] createArray(int size) {
        return new CommentModel[size];
    }

    protected void bindData(int position, View view, final CommentModel data) {

        // title
        TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
        titleView.setText(data.label);

        // body
        TextView bodyTextView = (TextView) view.findViewById(R.id.bodyTextView);
        bodyTextView.setText(HtmlHelper.fromHtml(data.comment_body));

        final View userIconLayout = view.findViewById(R.id.userLayout);
		final TextView usernameTextView = (TextView)view.findViewById(R.id.usernameTextView);
        
		UserModel userModel = new UserModel();
        userModel.uid = data.uuid;
        userModel.name = data.username;
        userModel.picture = data.picture;
        userIconLayout.setTag(R.id.user_icon_data, userModel);

		// username
		if (usernameTextView != null){
			usernameTextView.setText(data.username);
		}

        // modified  date
        if (data.changed != data.created) {
            TextView modifiedDateTextView = (TextView) view.findViewById(R.id.modifiedDateTextView);
            String changedDate = data.changed > 0 ? DateHelper.format(data.changed) : "";
            modifiedDateTextView.setText(changedDate);
        }

        // creation date
        TextView createdDateView = (TextView) view.findViewById(R.id.createdDateTextView);
        String createdDate = data.created > 0 ? DateHelper.format(data.created) : "";
        createdDateView.setText(createdDate);

        userIconLayout.setOnClickListener(new UserClickListener(context));
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

        final View selctedLayout = view.findViewById(R.id.selctedLayout);
        selctedLayout.setTag(R.id.current_item, data);
        if (selectedItems.contains(position)) {
            final float scale = view.getResources().getDisplayMetrics().density;
            selctedLayout.getLayoutParams().height = view.getHeight();
            selctedLayout.setVisibility(View.VISIBLE);

        } else {
            selctedLayout.setVisibility(View.GONE);
        }
    }

    protected void bindEvents(int position, View view, CommentModel data) {
        try {
            TextView bodyTextView = (TextView) view.findViewById(R.id.bodyTextView);
            TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
            titleView.setTag(R.id.forum_title, data);
            titleView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CommentModel data = (CommentModel) v.getTag(R.id.forum_title);
                    Intent intent = new Intent(context, EditCommentActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("commentId", data.cid);
                    Gson gson = new Gson();
                    bundle.putString("topic", gson.toJson(topicModel));
                    bundle.putString("comment", gson.toJson(data));
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            final View selctedLayout = view.findViewById(R.id.selctedLayout);
            final View itemLayout = view.findViewById(R.id.itemLayout);
			final View layout_forum_list_item = view.findViewById(R.id.layout_forum_list_item);
            final View mainView = view;
            final Integer currentPosition = position;
            final View userIconLayout = view.findViewById(R.id.userLayout);
            layout_forum_list_item.setOnTouchListener(createTimerTouchListener(layout_forum_list_item, selctedLayout, mainView));
            userIconLayout.setOnClickListener(new UserClickListener(context));

            selctedLayout.setOnTouchListener(createItemLayoutTouchListener(selctedLayout));
        } catch (Exception ex) {
            Log.e("CommentsAdapter", ex.getMessage());
            Log.e("CommentsAdapter", ex.getStackTrace().toString());
        }
    }
}