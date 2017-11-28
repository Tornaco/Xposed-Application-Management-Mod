package github.tornaco.xposedmoduletest.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by guohao4 on 2017/7/5.
 */

public class PinyinComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return Collator.getInstance(Locale.CHINESE).compare(o1, o2);
    }
}