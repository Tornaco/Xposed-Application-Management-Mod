package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.support.annotation.StringRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/28.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
@ToString
public class UserTipItem {
    @StringRes
    private int messageRes, titleRes;
}
