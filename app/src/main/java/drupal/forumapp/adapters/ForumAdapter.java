package drupal.forumapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import java.util.ArrayList;

import drupal.forumapp.R;
import drupal.forumapp.model.ForumModel;
import android.support.annotation.LayoutRes;
import android.widget.TextView;
import drupal.forumapp.model.ForumTopicStatisticModel;

import com.google.gson.Gson;

import drupal.forumapp.activities.ForumTopicsActivity;

public class ForumAdapter extends CustomBaseAdapter<ForumModel>{
	private ArrayList<ForumTopicStatisticModel> forumTopicsStatistics;
    public ForumAdapter(Context context, @LayoutRes int id, ArrayList<ForumModel> data, ArrayList<ForumTopicStatisticModel> forumTopicsStatistics) {
        super(context, id, data);
		this.forumTopicsStatistics = forumTopicsStatistics;
    }

    @Override
    public void clearSelection() {

    }

    public void setForumTopicsStatistics(ArrayList<ForumTopicStatisticModel> newValue){
        this.forumTopicsStatistics = newValue;
    }

    @Override
    protected ForumModel[] createArray(int size) {
        return new ForumModel[size];
    }

    protected void bindData(int position, View view, ForumModel data){
        TextView titleView = (TextView)view.findViewById(R.id.forumTitleTextView);
        titleView.setText(data.name);

        TextView postsCountTextView = (TextView)view.findViewById(R.id.postsNumberTextView);
        postsCountTextView.setText("" + data.num_posts);

        TextView topicsCountTextView = (TextView)view.findViewById(R.id.topicsNumberTextView);
        topicsCountTextView.setText("" + data.num_topics);

		TextView newPostsTextView = (TextView)view.findViewById(R.id.newTopicsCountTextView);
		View newTopicsLayout = view.findViewById(R.id.newTopicsLayout);
        newTopicsLayout.setVisibility(View.GONE);
		if (this.forumTopicsStatistics != null) {
			for(ForumTopicStatisticModel forumTopicStatisticModel: this.forumTopicsStatistics) {
				if (forumTopicStatisticModel.tid == data.tid && forumTopicStatisticModel.countNewNodes > 0) {
                    newTopicsLayout.setVisibility(View.VISIBLE);
					int newItems = forumTopicStatisticModel.countNewNodes;
                    newPostsTextView.setText("" + newItems);
				}
			}
		}
    }

    protected void bindEvents(int position, View view, ForumModel data){
        TextView titleView = (TextView)view.findViewById(R.id.forumTitleTextView);
        titleView.setTag(R.id.forum_title, data);
        titleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ForumModel data = (ForumModel)v.getTag(R.id.forum_title);
                Gson gson = new Gson();
                String forumJson = gson.toJson(data);

                Intent intent = new Intent(context, ForumTopicsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("forum", forumJson);
                intent.putExtras(bundle);
                context.startActivity(intent);
          }
        });;
    }
}