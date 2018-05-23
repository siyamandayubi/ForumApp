package drupal.forumapp.asyncTasks;

public interface DataListener<T>  {    
    public void onLoaded(T data);
}