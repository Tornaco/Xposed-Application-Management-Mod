package dev.tornaco.vangogh.display.appliers;

import android.support.annotation.NonNull;
import android.view.animation.Animation;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.media.Image;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/9/5.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class AnimationApplier implements ImageApplier {
    private Animation animation;

    @Override
    public void apply(@NonNull final ImageDisplayer displayer, @NonNull final Image image) {
        if (displayer.getView() == null) {
            displayer.display(image);
            return;
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
                displayer.display(image);
            }
        });
        displayer.getView().startAnimation(animation);
    }

    @Override
    public long duration() {
        return animation.getDuration();
    }
}
