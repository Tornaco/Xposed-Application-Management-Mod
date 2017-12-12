package github.tornaco.xposedmoduletest.model;

import lombok.Data;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/12.
 * Email: Tornaco@163.com
 */
@Data
@ToString
public class Permission {
    private String pkgName;
    private String permission;
    private String name;
    private int iconRes;
    private int state;
    private int category;
}
