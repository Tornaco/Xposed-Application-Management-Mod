# What is XAppGuard

## 1. Purpose
> 借助XPOSED实现系统级别应用锁。


## 2. Workflow
XAppGuardService(Framework)---XAppGuardApp(App)--User-(返回验证结果)


## 3. Func design
XAppGuardService(Framework)---XAppGuardApp(App)--User(返回验证结果)

### 3.1 XAppGuardService注入
```AMS```启动完成后添加```XAppGuardService```到```ServiceManager```

### 3.2 XAppGuardService获取
```java
IAppGuardService.Stub.asInterface(ServiceManager.getService(XContext.APP_GUARD_SERVICE));
```

### 3.3 劫持的AMS方法
1. Hook start方法，初始化我们的服务。
```java
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    mAppGuardService = new XAppGuardService(context);
                    mAppGuardService.publish();
                }
            });
            XposedBridge.log(TAG + "hookAMSStart OK");
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hook hookAMSStart");
            xStatus = XStatus.ERROR;
        }
```

2. Hook systemReady，初始化我们的服务。

```java
        private void hookAMSSystemReady(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "hookAMSSystemReady...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (mAppGuardService != null) {
                        mAppGuardService.systemReady();
                        mAppGuardService.setStatus(xStatus);
                    }
                }
            });
            XposedBridge.log(TAG + "hookAMSSystemReady OK");
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookAMSSystemReady");
            xStatus = XStatus.ERROR;
        }
    }
```

3. Hook shutdown 做保存工作。
```java
        private void hookAMSShutdown(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "hookAMSShutdown...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "shutdown", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (mAppGuardService != null) {
                        mAppGuardService.shutdown();
                    }
                }
            });
            XposedBridge.log(TAG + "hookAMSShutdown OK");
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookAMSShutdown");
            xStatus = XStatus.ERROR;
        }
    }
```

4. Hook startActivity方法，插入我们的逻辑。
```java
private void hookActivityStarter(XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            Method startActivityLockedExact = null;
            int matchCount = 0;
            for (Method method : Class.forName("com.android.server.am.ActivityStarter",
                    false, lpparam.classLoader).getDeclaredMethods()) {
                if (method.getName().equals("startActivityLocked")) {
                    startActivityLockedExact = method;
                    startActivityLockedExact.setAccessible(true);
                    matchCount++;

                    Class[] classes = method.getParameterTypes();
                    for (int i = 0; i < classes.length; i++) {
                        if (ActivityOptions.class == classes[i]) {
                            activityOptsIndex = i;
                        }
                    }
                }
            }
   ....
```

5. Hook moveTask方法，在最近任务植入逻辑。
```java
private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
            final Method moveToFront = Class.forName("com.android.server.am.ActivityStackSupervisor",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("findTaskToMoveToFrontLocked",
                            taskRecordClass, int.class, ActivityOptions.class,
                            String.class, boolean.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param)
                        throws Throwable {
                    super.beforeHookedMethod(param);
...
```

### 3.4 劫持指纹服务
应用不在前台无法使用指纹，因此需要劫持。
```java
    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "hookFPService...");
        try {
            XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintService", lpparam.classLoader),
                    "canUseFingerprint", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object pkg = param.args[0];
                            if (BuildConfig.APPLICATION_ID.equals(pkg)) {
                                param.setResult(true);
                                XposedBridge.log(TAG + "ALLOWING APPGUARD TO USE FP ANYWAY");
                            }
                        }
                    });
            XposedBridge.log(TAG + "hookFPService OK");
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookFPService" + e);
            if (xStatus != XStatus.ERROR) xStatus = XStatus.WITH_WARN;
        }
    }
```

### 3.5 App封装的XAppGuardManager
方便APP直接调用服务。


## 4. Build depencency
依赖hiddenapi，Xposed-Framework。

## 5. Test
[查看最新测试报告](https://github.com/Tornaco/XAppGuard/blob/master/TestResults-XAppGuardManagerTest.html /"最新测试报告")
测试代码位于```androidTest```目录下。


## 6. Demo

![demo](art/videos/workflow.gif)
