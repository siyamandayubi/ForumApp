package drupal.forumapp.loaders;

import java.util.ArrayList;
import java.util.HashMap;

import drupal.forumapp.model.ForumTopicStatisticListModel;
import drupal.forumapp.model.ForumTopicStatisticModel;

public class LoaderBaseResponse<T>{

	private HashMap<String, Object> additionalData;
	public T mainOutput;
	public ForumTopicStatisticListModel forumTopicsStatistics;

	public static final String FORUM_KEY = "Forum";

	public LoaderBaseResponse(){
		additionalData = new HashMap<String, Object>();
	}

	public Object getData(String key){
		if (additionalData.containsKey(key)){
			return additionalData.get(key);
		}

		return null;
	}

	public void setData(String key, Object data){
		additionalData.put(key, data);
	}
}