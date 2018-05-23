package drupal.forumapp.domain;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import android.support.v4.app.NotificationCompat.Builder;
import android.content.Intent;
import android.app.PendingIntent ;
import android.app.NotificationManager;

import drupal.forumapp.R;
import drupal.forumapp.activities.FavoritesActivity;
import drupal.forumapp.activities.NewTopicsActivity;

/**
 * Created by serva on 10/21/2017.
 */

public class NotificationHelper {
    public static void createNotification(Context context, String title, String text, int id){
        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.add_button_shape)
            .setContentTitle(title)
            .setContentText(text);
            
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(context, NewTopicsActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent pendingIntent =
            PendingIntent.getActivity(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Puts the PendingIntent into the notification builder
        builder.setContentIntent(pendingIntent);

        // Notifications are issued by sending them to the
        // NotificationManager system service.
        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds an anonymous Notification object from the builder, and
        // passes it to the NotificationManager
        mNotificationManager.notify(id, builder.build());
    }
}
