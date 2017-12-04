package tornaco.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class Use {

    public static void main(String... args) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

        MultData data = new MultData(20, "Yinheng");

        Method method = data.getClass().getMethod("getAge");
        method.setAccessible(true);

        Object ageObject = method.invoke(data);

        System.out.println(ageObject);
    }
}
