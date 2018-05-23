package drupal.forumapp.common;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by serva on 11/5/2017.
 */

public abstract class TouchTimer implements View.OnTouchListener {
    private View view;
    private float initialTouchX;
    private float initialTouchY;
    private @LayoutRes
    Integer originalRes;
    private AnimationDrawable animationDrawable;
    private @LayoutRes
    Integer animationRes;
    private Runnable runnable;
    private Boolean isClick;

    public TouchTimer(View view, @LayoutRes Integer originalRes, @LayoutRes Integer animationRes) {
        this.view = view;
        this.animationRes = animationRes;
        this.originalRes = originalRes;
        this.animationDrawable = (AnimationDrawable) view.getBackground();
        GradientDrawable frame = (GradientDrawable) this.animationDrawable.getFrame(0);
        this.isClick = false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        boolean animationDrawableStart = false;
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.isClick = true;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        isClick = false;
                        if (animationDrawable != null) {
                            animationDrawable.stop();
                            animationDrawable.selectDrawable(0);
                        }
                        onLongTouchEnded();
                    }
                };
                view.getHandler().postDelayed(runnable, 1500);
                if (animationDrawable != null) {
                    //animationDrawable.setEnterFadeDuration(2000);
                    //animationDrawable.setExitFadeDuration(200);
                    animationDrawableStart = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                stop();
                if (this.isClick) {
                    onShortTouchEnded();
                    this.isClick = false;
                }
                return true;

            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_MOVE:
                if (action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_CANCEL){
                    this.isClick = false;
                }
                float xChange = Math.abs(initialTouchX - event.getRawX());
                float yChange = Math.abs(initialTouchY - event.getRawY());
                Log.d("TouchTimer", event.getRawX() + "  " + event.getRawY());
                Log.d("TouchTimer", initialTouchX + "  " + initialTouchY);
                Log.d("TouchTimer", xChange + "  " + yChange);
                if (xChange > 20 || yChange > 20) {
                    this.isClick = false;
                    return stop();
                } else {
                    return false;
                }
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_CANCEL:
                return stop();
            default:
                Log.d("Motion", event.getAction() + "");
        }

        if (animationDrawableStart) {
            animationDrawable.start();
            return true;
        } else {
            return false;
        }
    }

    protected Boolean stop() {
        view.getHandler().removeCallbacks(runnable);
        if (animationDrawable != null) {
            animationDrawable.stop();
            animationDrawable.selectDrawable(0);
        }

        return true;
    }

    protected abstract void onLongTouchEnded();

    protected void onShortTouchEnded() {

    }

}
