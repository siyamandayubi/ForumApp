package drupal.forumapp.adapters;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import drupal.forumapp.R;
import drupal.forumapp.activities.UserActivity;
import drupal.forumapp.common.DataSelectedListener;
import drupal.forumapp.common.TouchTimer;
import drupal.forumapp.loaders.ForumsLoader;
import drupal.forumapp.model.ForumList;
import drupal.forumapp.model.ForumModel;
import drupal.forumapp.model.UserModel;

public abstract class CustomBaseAdapter<T> extends BaseAdapter {
    protected Context context;
    protected ArrayList<T> data;
    protected DataSelectedListener dataSelectedListener;
    protected HashSet<Integer> selectedItems;
    protected @LayoutRes Integer touchableOriginalRes;
    protected @LayoutRes Integer touchableAnimationRes;
    protected Map<Integer, Integer> selectedItemsHeight = new HashMap<Integer, Integer>();

    @LayoutRes
    private final int rowId;
    private static LayoutInflater inflater = null;

    public CustomBaseAdapter(Context context, @LayoutRes int id, ArrayList<T> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        this.rowId = id;
        selectedItems = new HashSet<Integer>();
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    public T getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null) {
            vi = inflater.inflate(this.rowId, null);
            bindEvents(position, vi, data.get(position));
        }

        bindData(position, vi, data.get(position));
        return vi;
    }


    protected void selectionChanged() {
        T[] selectedData;
        selectedData = createArray(selectedItems.size());
        int counter = 0;
        for (int i : selectedItems) {
            selectedData[counter] = data.get(i);
            counter++;
        }

        Integer[] indexes = new Integer[selectedItems.size()];
        indexes = selectedItems.toArray(indexes);
        dataSelectedListener.onSelect(indexes, selectedData);
    }

    @NonNull
    protected View.OnTouchListener createItemLayoutTouchListener(final View selctedLayout) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    T selectedItem =(T)selctedLayout.getTag(R.id.current_item);
                    int position = data.indexOf(selectedItem);
                    selectedItemsHeight.remove(position);
                    selctedLayout.setVisibility(View.GONE);
                    if (selectedItems.contains(position)) {
                        selectedItems.remove(position);
                    }
                    selectionChanged();
                }
                return true;
            }

        };
    }

    @NonNull
    protected TouchTimer createTimerTouchListener(final View view, final View selctedLayout, final View mainView) {
        return new TouchTimer(view, touchableOriginalRes , touchableAnimationRes) {
            @Override
            protected void onLongTouchEnded() {
                Integer height = mainView.getHeight();
                selctedLayout.getLayoutParams().height = height;
                T selectedItem =(T)selctedLayout.getTag(R.id.current_item);
                int position = data.indexOf(selectedItem);
                selectedItemsHeight.put(position, height);
                selctedLayout.setVisibility(View.VISIBLE);
                if (!selectedItems.contains(position)) {
                    selectedItems.add(position);
                }

                selectionChanged();
            }

			@Override
			protected void onShortTouchEnded(){
                showMenu(view);
			}
        };
    }

	protected void showMenu(View view){

    }

    public void setDataSelectedListener(DataSelectedListener dataSelectedListener) {
        this.dataSelectedListener = dataSelectedListener;
    }

    public void clearSelection() {
        selectedItems.clear();
        selectionChanged();
        this.notifyDataSetChanged();
    }

    protected abstract T[] createArray(int size);

    protected abstract void bindData(int position, View view, T data);

    protected abstract void bindEvents(int position, View view, T data);

    public class UserClickListener implements View.OnClickListener {

        private final Context context;

        public UserClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            UserModel user = (UserModel) view.getTag(R.id.user_icon_data);
            if(user == null){
                return;
            }

            Intent intent = new Intent(context, UserActivity.class);
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            bundle.putString("user", userJson);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}