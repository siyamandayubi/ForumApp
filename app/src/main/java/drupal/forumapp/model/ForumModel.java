package drupal.forumapp.model;

/**
 * Created by serva on 10/15/2017.
 */

public class ForumModel {
    public int tid;
    public int vid;
    public String name;
    public String description;
    public String format;
    public int weight;
    public int depth;
    public int num_topics;
    public int num_posts;
    public PostInfo last_post;
    public class PostInfo{
        public String name;
        public int uid;
        public long created;
    }
}
