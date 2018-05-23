package drupal.forumapp.model;

import java.util.List;

/**
 * Created by serva on 11/25/2017.
 */

public class FavoriteTopicModel {
    public int nid;

    public static FavoriteTopicModel find(List<FavoriteTopicModel> list, int nid) {
        FavoriteTopicModel found = null;
        for (int i = 0; i < list.size(); i++) {
            FavoriteTopicModel temp = list.get(i);
            if (temp.nid == nid) {
                return temp;
            }
        }

        return null;
    }
}
