package drupal.forumapp.common;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import drupal.forumapp.R;

public class AnimationHelper {
    public static void expandToolbar(Context context, View view) {
        //view.setTranslationY(0);
        expandToolbar(context, view, 0);
    }

    public static void expandToolbar(Context context, View view, long duration) {
        //view.setTranslationY(0);
        final View viewW = view;
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.toolbar_slide_in);
        if (duration > 0) {
            animation.setDuration(duration);
        }
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation);
    }

    public static void collapseToolbar(Context context, final View oldView, View newView) {
        final View oldViewW = oldView;
        final View newViewW = newView;
        final Context contextW = context;
        Animation firstAnimation = AnimationUtils.loadAnimation(context, R.anim.toolbar_slide_out);
        final long duration = firstAnimation.getDuration();
        firstAnimation.setDuration(duration);

        Animation secondAnimation = AnimationUtils.loadAnimation(context, R.anim.toolbar_slide_in_bottom);
        newView.setVisibility(View.VISIBLE);
        newView.startAnimation(secondAnimation);
        firstAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (oldView != null) {
                    oldViewW.setVisibility(View.GONE);
                }
                // expandToolbar(contextW, newViewW, duration);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (oldView != null) {
            oldView.startAnimation(firstAnimation);
        }
    }

    public static AnimationDrawable fillFrames(AnimationDrawable animationDrawable) {
        AnimationDrawable newAnimationDrawable = new AnimationDrawable();
        for (int i = 0; i < animationDrawable.getNumberOfFrames() - 1; i++) {
            GradientDrawable currentFrame = (GradientDrawable) animationDrawable.getFrame(i);
            newAnimationDrawable.addFrame(currentFrame, animationDrawable.getDuration(i));
            if (i == animationDrawable.getNumberOfFrames() - 1) {
                break;
            }

            GradientDrawable nextFrame = (GradientDrawable) animationDrawable.getFrame(i + 1);
            int duration = animationDrawable.getDuration(i);
            int totalDurations = 0;
            while (totalDurations > duration) {
                totalDurations += 10;
                GradientDrawable gradientDrawable = new GradientDrawable();

                newAnimationDrawable.addFrame(gradientDrawable, 10);
            }
        }

        return animationDrawable;
    }
}