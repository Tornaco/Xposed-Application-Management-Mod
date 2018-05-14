package github.tornaco.xposedmoduletest.xposed.repo;

import java.util.Map;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public interface MapRepo<K, V> extends Map<K, V> {

    void reload();

    void reloadAsync();

    void flush();

    void flushAsync();

    String name();

    Map<K, V> dup();

    boolean hasNoneNullValue(K k);
}
