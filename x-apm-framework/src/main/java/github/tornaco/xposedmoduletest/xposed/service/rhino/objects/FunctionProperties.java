package github.tornaco.xposedmoduletest.xposed.service.rhino.objects;

import android.text.TextUtils;

import org.mozilla.javascript.ImporterTopLevel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tornaco on 2018/6/15 11:21.
 * This file is writen for project X-APM at host guohao4.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface FunctionProperties {

    String funcName() default "";

    interface ClazzParser {

        static String[] getAllFunctionProperties(Class<? extends ImporterTopLevel> clazz) {
            Method[] methods = clazz.getDeclaredMethods();
            List<String> funcs = new ArrayList<>(methods.length);
            for (Method m : methods) {
                FunctionProperties p = m.isAnnotationPresent(FunctionProperties.class)
                        ? m.getAnnotation(FunctionProperties.class) : null;
                if (p != null) {
                    String prop = TextUtils.isEmpty(p.funcName()) ? m.getName() : p.funcName();
                    funcs.add(prop);
                }
            }
            String[] arr = new String[funcs.size()];
            return funcs.toArray(arr);
        }
    }
}
