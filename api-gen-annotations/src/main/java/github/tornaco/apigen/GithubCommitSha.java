package github.tornaco.apigen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Nick@NewStand.org on 2017/4/12 13:12
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GithubCommitSha {
    String user() default "Tornaco";

    String repo() default "X-APM";

    String UNKNOWN = "unknown";
}
