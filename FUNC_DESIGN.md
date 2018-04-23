## 基本思路

通过ServiceManager植Android framework一个基于Binder的服务，作为基本的管理者。

![LOGO](/art/func_design/FuncDesign_Overview.png)

### 服务植入

当Android启动时，会启动```ActivityManagerService```（以下简称AMS），我们在```AMS```的```start```方法里注入我们的服务：
```java
    XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    getBridge().attachContext(context);
                    getBridge().publish();

                    ...
                }
            });
```

在ServiceManager中添加相应的Binder服务，X-APM的服务注入：
```java
    @Override
    @CommonBringUpApi
    public void publish() {
        try {
            String serviceName = XAshmanManager.SERVICE_NAME;
            XposedLog.boot("publishing ash to: " + serviceName);
            ServiceManager.addService(serviceName, asBinder());
        } catch (Throwable e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
        construct();

        mAppGuardService.publish();
    }
```

注意，由于Android O开始对```addService```的服务参数，做了更严格的selinux检查，因此在Android O上已经不允许添加自定义的Binder服务，因此我们借用了TV服务：

```java
public static final String SERVICE_NAME =
            OSUtil.isOOrAbove() ? Context.TV_INPUT_SERVICE : "user.tor_ash";
````

### 应用管理应用端与注入的服务通信

通过aidl方式进行通信，首先通过ServiceManager取得服务，参考封装好的```XAshmanManager```
```java
    public void retrieveService() {
        mService = IAshmanService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
    }
```
随后可以通过```mService```与服务端通信，以注入电源按钮事件API为例：
```java
    public void injectPowerEvent() {
        ensureService();
        try {
            mService.injectPowerEvent();
        } catch (Exception e) {

        }
    }
```
