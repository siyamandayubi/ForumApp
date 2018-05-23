package drupal.forumapp.domain;

import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;

/**
 * Created by serva on 10/21/2017.
 */

public class HtmlHelper {
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        try {
            return source != null? Html.fromHtml(source): null;
        }
        catch (Exception ex){
            Log.e("HtmlHelper", ex.getMessage());
            Log.e("HtmlHelper", ex.getStackTrace().toString());
            return Spannable.Factory.getInstance().newSpannable(source +"---" + Html.fromHtml(ex.getMessage()));
        }
    }
}
