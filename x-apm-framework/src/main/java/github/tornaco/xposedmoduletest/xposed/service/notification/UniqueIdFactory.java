package github.tornaco.xposedmoduletest.xposed.service.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Tornaco on 2018/5/30 10:10.
 * This file is writen for project X-APM at host guohao4.
 */
public abstract class UniqueIdFactory {

    private final static AtomicInteger ID = new AtomicInteger(0);

    private final static Map<Object, Integer> ID_MAP = new HashMap<>();

    public static int getNextId() {
        return ID.getAndIncrement();
    }

    public static int getIdByTag(Object tag) {
        Integer id = ID_MAP.get(tag);
        if (id == null) {
            id = getNextId();
            ID_MAP.put(tag, id);
        }
        return id;
    }
}
