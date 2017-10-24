# What is XAppGuard

## Purpose

借助XPOSED实现系统级别应用锁。

## Workflow

XAppGuardService(Framework)---XAppGuardApp(App)--User-(返回验证结果)

## Func design

XAppGuardService(Framework)---XAppGuardApp(App)--User(返回验证结果)

### XAppGuardService注入
```AMS```启动完成后添加```XAppGuardService```到```ServiceManager```

### XAppGuardService获取
```java
IAppGuardService.Stub.asInterface(ServiceManager.getService(XContext.APP_GUARD_SERVICE));
```

### 劫持的AMS方法
```java
 hookAMSStart(lpparam);
 hookSystemServiceRegister(lpparam);
 hookAMSSystemReady(lpparam);
 hookAMSShutdown(lpparam);
 hookActivityStarter(lpparam);
 hookTaskMover(lpparam);
```

### 劫持指纹服务
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

### App封装的XAppGuardManager
方便APP直接调用服务。
