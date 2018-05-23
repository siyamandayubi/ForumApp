package drupal.forumapp;


import android.app.Application;
import android.content.Intent;
import java.util.HashMap;

import drupal.forumapp.services.RefreshDataService;
import drupal.forumapp.services.Util;
import drupal.forumapp.model.CacheModel;
import java.util.Calendar;
import java.util.Date;
/**
 * Created by serva on 10/29/2017.
 */

public class App extends Application {
    
	private HashMap<String, Object> cache = new HashMap<String, Object>();

	@Override
    public void onCreate(){
        super.onCreate();
        Util.scheduleJob(this);
        startService(new Intent(this, RefreshDataService.class));
    }

	public Object getCacheItem(String key){
		CacheModel returnValue = (CacheModel)cache.get(key);

        Calendar expiration = Calendar.getInstance();
		expiration.add(Calendar.MINUTE, 5);
		if(returnValue != null ){
			if (expiration.after(returnValue.createdTime)){
				return returnValue.data;
			}
			else{
				cache.remove(key);
			}
		}

		return null;
	}

	public void putCacheItem(String key, Object data){
		CacheModel cacheItem = new CacheModel();
		cacheItem.data = data;
		cacheItem.createdTime = Calendar.getInstance();
		cache.put(key, cacheItem);
	}
}
