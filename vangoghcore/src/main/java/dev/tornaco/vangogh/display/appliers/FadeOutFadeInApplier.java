package dev.tornaco.vangogh.display.appliers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateInterpolator;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public class FadeOutFadeInApplier implements ImageApplier {

    private long duration;

    public FadeOutFadeInApplier() {
        this(500);
    }

    public FadeOutFadeInApplier(long duration) {
        this.duration = duration;
    }

    @Override
    public void apply(@NonNull final ImageDisplayer displayer, @NonNull final Image image) {
        if (displayer.getView() == null) {
            displayer.display(image);
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(displayer.getView(), "alpha", 1f, 0f);
        animator.setDuration(150);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                displayer.display(image);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(displayer.getView(), "alpha", 0f, 1f)
                        .setDuration(duration);
                fadeIn.setInterpolator(new AccelerateInterpolator());
                fadeIn.start();

            }
        });
        animator.start();
    }

    @Override
    public long duration() {
        return duration;
    }
}
