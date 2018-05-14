package github.tornaco.xposedmoduletest.xposed.service;

import android.os.Bundle;

import lombok.Builder;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@Builder
public class VerifyArgs {
    public Bundle bnds;
    public String pkg;
    public int uid;
    public int pid;
    public boolean injectHomeOnFail;
    public VerifyListener listener;
}