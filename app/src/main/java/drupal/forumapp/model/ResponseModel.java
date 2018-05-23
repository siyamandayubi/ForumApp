package drupal.forumapp.model;

/**
 * Created by serva on 10/20/2017.
 */

public class ResponseModel<T> {
    public int statusCode;
    public String error;
    public T result;
    public String rawResult;
}
