package github.tornaco.xposedmoduletest.ui.widget;

import android.support.annotation.NonNull;

import com.vanniktech.emoji.EmojiProvider;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.emoji.EmojiCategory;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.util.EmojiUtil;

/**
 * Created by guohao4 on 2018/1/30.
 * Email: Tornaco@163.com
 */

public class SimpleEmojiProvider implements EmojiProvider {
    @NonNull
    @Override
    public EmojiCategory[] getCategories() {
        return new EmojiCategory[]{
                new EmojiCategory() {
                    @NonNull
                    @Override
                    public Emoji[] getEmojis() {
                        return new Emoji[]{
                                new Emoji(EmojiUtil.HAPPY, R.drawable.smile),
                                new Emoji(EmojiUtil.DOG, R.drawable.d_doge),
                                new Emoji(EmojiUtil.HEART, R.drawable.tieba_emotion_heart),
                                new Emoji(EmojiUtil.HEART_BREAK, R.drawable.tieba_emotion_heart_break),
                        };
                    }

                    @Override
                    public int getIcon() {
                        return 0;
                    }
                }
        };
    }
}
