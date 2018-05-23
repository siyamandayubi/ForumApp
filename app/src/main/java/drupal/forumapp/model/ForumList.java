package drupal.forumapp.model;

import java.util.HashMap;

/**
 * Created by serva on 10/15/2017.
 */
public class ForumList extends BaseModel {
    public ForumList(){
        forums = new HashMap<String, ForumModel>();
    }

    public HashMap<String, ForumModel> forums;
}

