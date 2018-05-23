package drupal.forumapp.model;

public class TopicModel {
    public static final int COMMENTS_OPEN = 2;
    public static final int COMMENTS_CLOSED = 1;
    public static final int COMMENTS_HIDDEN = 0;
    
    public int nid;
    public int forum_tid;
    public String title;
    public String name;
    public String body;
    public PictureModel picture;
    public int comment_count;
    public Long created;
    public int uuid;
    public int comment;
    public Long changed;
    public int status;
    public int vid;
    public int id;
    public int last_comment_uid;
    public Long last_comment_timestamp;

    // this field is used only in add mode
    public int tid;
    public transient String localUserImagePath;
}