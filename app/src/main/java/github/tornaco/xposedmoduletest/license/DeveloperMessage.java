package github.tornaco.xposedmoduletest.license;

import lombok.Data;

/**
 * Created by guohao4 on 2018/1/29.
 * Email: Tornaco@163.com
 */
@Data
public class DeveloperMessage {
    private String title;
    private String message;
    private long timeMills;
    private String messageId;
    private String[] payload;
    private boolean cancelable;
    private int importance;
}
