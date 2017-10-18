package dev.tornaco.vangogh.request;

import java.util.Observable;

import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/8/28.
 * Email: Tornaco@163.com
 */

public class ImageManager extends Observable {

    @AllArgsConstructor
    @Getter
    public class ImageArgs {
        private ImageSource source;
        private Image image;
    }

    @Getter
    private static ImageManager instance = new ImageManager();

    void onImageReadyToDisplay(ImageSource source, Image image) {
        setChanged();
        notifyObservers(new ImageArgs(source, image));
    }
}
