package drupal.forumapp.common;

import android.text.Html;

import  drupal.forumapp.model.CommentModel;
import  drupal.forumapp.model.TopicModel;
import  drupal.forumapp.model.ForumModel;
import drupal.forumapp.domain.HtmlHelper;

public class ShareHelper {
    public static String getToSharedString(Object object){
        if (object instanceof CommentModel){
            return convert((CommentModel)object);
        }
        else if (object instanceof TopicModel){
            return convert((TopicModel)object);
        }
        else if (object instanceof ForumModel){
            return convert((ForumModel)object);
        }

        return "";
    }

    private static String convert(CommentModel comment){
        String output = String.format("%1$s\n%2$s", comment.label, comment.comment_body);
        return Html.escapeHtml(output).toString();
    }

    private  static String convert(TopicModel topic){
        String output = String.format("%1$s\n/%2$s", topic.title, topic.body);
        return Html.escapeHtml(output).toString();
  }

    private  static String convert(ForumModel forum){
        String output = String.format("<h2>%1$s</h2><div>/%2$s</div>", forum.name, forum.description);                
        return HtmlHelper.fromHtml(output).toString();
    }
}