package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import lombok.Data;

/**
 * Created by Tornaco on 2018/4/29 12:21.
 * God bless no bug!
 */
@Data
public class GcmEvent {
    private long eventTime;
    private String eventPackage;
}
