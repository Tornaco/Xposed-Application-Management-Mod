package github.tornaco.xposedmoduletest.xposed.service.am;

/**
 * Created by Tornaco on 2018/4/28 10:16.
 * God bless no bug!
 */
public interface ActiveServicesServiceStopper {
    boolean stopService(ServiceRecordProxy serviceRecordProxy);
}
