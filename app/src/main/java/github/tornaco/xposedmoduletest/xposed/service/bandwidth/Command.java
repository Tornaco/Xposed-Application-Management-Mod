package github.tornaco.xposedmoduletest.xposed.service.bandwidth;

import com.google.common.collect.Lists;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Created by guohao4 on 2017/12/5.
 * Email: Tornaco@163.com
 */
@Getter
public class Command {

    private String cmd;

    private ArrayList<Object> args = Lists.newArrayList();

    public Command(String cmd, Object... args) {
        this.cmd = cmd;
        for (Object a : args) {
            appendArg(a);
        }
    }

    public Command appendArg(Object ar) {
        args.add(ar);
        return this;
    }
}
