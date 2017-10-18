// IAppService.aidl
package github.tornaco.xposedmoduletest;

import github.tornaco.xposedmoduletest.ICallback;

interface IAppService {
    void noteAppStart(in ICallback callback, String pkg, int callingUID, int callingPID);
}
