package dev.tornaco.vangogh.request;

import org.newstand.logger.Logger;

import dev.tornaco.vangogh.media.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
class DisplayRequest {
    private Image image;
    @Delegate
    private ImageRequest imageRequest;
    private Object arg;

    long getPossibleDurationOpt() {
        long timeTaken = 0;
        if (imageRequest.getApplier() != null && arg == null) {
            timeTaken = imageRequest.getApplier().duration();
        }
        if (timeTaken > 120) timeTaken = 120;
        return timeTaken;
    }

    long callApply() {
        Logger.v("DisplayRequest, run, arg: %s", arg);
        long timeTaken = 52;
        if (imageRequest.getApplier() != null && arg == null) {
            imageRequest.getApplier().apply(imageRequest.getDisplayer(), image);
            timeTaken = imageRequest.getApplier().duration();
        } else {
            imageRequest.getDisplayer().display(image);
        }
        return timeTaken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayRequest that = (DisplayRequest) o;

        return imageRequest.equals(that.imageRequest);

    }

    @Override
    public int hashCode() {
        return imageRequest.hashCode();
    }
}
