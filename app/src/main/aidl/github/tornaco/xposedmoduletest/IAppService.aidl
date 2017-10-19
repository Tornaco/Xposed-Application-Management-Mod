// IAppService.aidl
package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.IXModuleToken;

interface IAppService {
    void noteAppStart(in ICallback callback, String pkg, int callingUID, int callingPID);
    void onHome();
    void registerXModuleToken(in IXModuleToken token);
}
