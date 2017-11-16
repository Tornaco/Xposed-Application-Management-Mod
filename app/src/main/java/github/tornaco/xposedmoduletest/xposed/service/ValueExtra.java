package github.tornaco.xposedmoduletest.xposed.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/11/16.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
class ValueExtra<X, T> {
    private X value;
    private T extra;
}
