package dev.tornaco.vangogh.display.appliers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

public class ScaleInXYApplier implements ImageApplier {
    @Override
    public void apply(@NonNull final ImageDisplayer displayer, @NonNull final Image image) {
        if (displayer.getView() == null) {
            displayer.display(image);
            return;
        }

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(displayer.getView(), "scaleX", 1f, 0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(displayer.getView(), "scaleY", 1f, 0f);
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(animatorX, animatorY);
        animator.setDuration(150);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                displayer.display(image);
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(displayer.getView(), "scaleX", 0f, 1f);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(displayer.getView(), "scaleY", 0f, 1f);
                AnimatorSet animator = new AnimatorSet();
                animator.playTogether(animatorX, animatorY);
                animator.start();
            }
        });
        animator.start();
    }

    @Override
    public long duration() {
        return 500;
    }
}
