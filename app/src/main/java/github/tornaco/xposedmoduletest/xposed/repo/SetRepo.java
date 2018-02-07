package github.tornaco.xposedmoduletest.xposed.repo;

import java.util.Set;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public interface SetRepo<T> {

    Set<T> getAll();

    void reload();

    void reloadAsync();

    void flush();

    void flushAsync();

    boolean add(T t);

    boolean remove(T t);

    void removeAll();

    boolean has(T t);
    boolean has(T[] t);

    String name();

    int size();
}
