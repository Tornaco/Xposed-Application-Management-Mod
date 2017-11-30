package github.tornaco.xposedmoduletest.util;

/**
 * Created by guohao4 on 2017/11/15.
 * Email: Tornaco@163.com
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class ViewAnimatorUtil {

    public static final int DURATION_SHORT = 250;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void circularHide(final View view, Animator.AnimatorListener listener) {
        if (view.getWindowToken() == null) return;

        // getSingleton the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        // getSingleton the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });

        if (listener != null) {
            anim.addListener(listener);
        }

        anim.setDuration(500);

        anim.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void circularShow(final View view, final Runnable runnable) {
        // getSingleton the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        // getSingleton the final radius for the clipping circle
        int finalRadius = view.getWidth();

        // create and start the animator for this view
        // (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (runnable != null) runnable.run();
            }
        });

        anim.setDuration(500);

        anim.start();
    }

    public static void animateTextChange(final TextView view, final int toText, final Runnable rWhenEnd) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        final ObjectAnimator restore = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(DURATION_SHORT);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        restore.setDuration(DURATION_SHORT);
        restore.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setText(toText);
                restore.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setText(toText);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Do nothing.
            }
        });
        if (rWhenEnd != null)
            restore.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rWhenEnd.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    rWhenEnd.run();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        alpha.start();
    }
}