package drupal.forumapp.common;

public interface DataSelectedListener<T>  {    
    public void onSelect(Integer[] selectedIndexes, T[] selectedData);
}