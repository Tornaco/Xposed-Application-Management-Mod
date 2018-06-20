package github.tornaco.xposedmoduletest.xposed.repo;

import java.util.Collection;
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

    boolean addAll(Collection<? extends T> c);

    boolean remove(T t);

    void removeAll();

    boolean has(T t);

    // Note, Array element may be null
    boolean has(T[] t);

    String name();

    int size();
}
