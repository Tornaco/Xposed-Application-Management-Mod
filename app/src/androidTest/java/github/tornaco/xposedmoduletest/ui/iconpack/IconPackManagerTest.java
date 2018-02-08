package github.tornaco.xposedmoduletest.ui.iconpack;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;
import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;

/**
 * Created by guohao4 on 2018/2/8.
 * Email: Tornaco@163.com
 */
public class IconPackManagerTest {
    @Test
    public void getAvailableIconPacks() throws Exception {
        List<IconPack> list = IconPackManager.getAvailableIconPacks(InstrumentationRegistry.getTargetContext());
        Logger.d("list size: " + list);
        Collections.consumeRemaining(list,
                new Consumer<IconPack>() {
                    @Override
                    public void accept(IconPack iconPack) {
                        Logger.d(iconPack.label);
                        Logger.d(iconPack.packageName);
                    }
                });
    }

}