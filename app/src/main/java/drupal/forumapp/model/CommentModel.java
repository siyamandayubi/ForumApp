package drupal.forumapp.model;

/**
 * Created by serva on 10/15/2017.
 */

public class CommentModel {
    public int cid;
    public String comment_body;
    public Long changed;
    public int status;
    public String node_type;
    public Long created;
    public String name;
	public String username;
    public String label;
    public int uuid;
    public int nid;
    public PictureModel picture;
    public transient String localUserImagePath;
}