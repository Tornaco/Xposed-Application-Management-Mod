package dev.tornaco.vangogh.display.appliers;

import android.content.Context;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import dev.tornaco.vangogh.display.ImageApplier;
import dev.tornaco.vangogh.display.ImageDisplayer;
import dev.tornaco.vangogh.media.Image;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/9/5.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class ResAnimationApplier implements ImageApplier {
    private Context context;
    @AnimRes
    private int animRes;

    @Override
    public void apply(@NonNull final ImageDisplayer displayer, @NonNull final Image image) {
        Animation animation = AnimationUtils.loadAnimation(context, animRes);
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
        return 0;
    }
}
