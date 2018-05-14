package github.tornaco.xposedmoduletest.xposed.bean;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Tornaco on 2018/4/3 12:00.
 * God bless no bug!
 */
@Getter
@Builder
public class TypePack {
    private int int1, int2, int3;
    private boolean boolean1, boolean2, boolean3;
    private Object o1, o2, o3;
    private String s1, s2, s3;
}
