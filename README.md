## Play版下载

<a href="https://play.google.com/store/apps/details?id=github.tornaco.xposedmoduletest"><img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="48"></a>

## 开发与交流

[TG群组](https://t.me/xposed_apm_mod)

## 使用教程

[Wiki中文](https://github.com/Tornaco/X-APM/wiki)

## 编译状态

[![Build Status](https://travis-ci.org/Tornaco/X-APM.svg?branch=master)](https://travis-ci.org/Tornaco/X-APM)

## 已知问题
1. 8.x通知权限。
2. 8.x指纹震动。
3. Android P禁止了hidden api的使用，需要寻找解决方案。
4. ~~Android O后台模糊（TaskSnapshot如何处理？）。~~

## 4. 编译
依赖```hiddenapi```，```Xposed-Framework```，更详细的[编译步骤](https://github.com/Tornaco/X-APM/tree/master/build_var_controls)。

## 5. 软件设计思路
开发者可以查看本应用的[设计细节](https://github.com/Tornaco/X-APM/blob/master/FUNC_DESIGN.md)。

## 6. 开发者API（SDK）
开发者可以使用X-APM提供的SDK来配置X-APM，SDK提供了所有可与X-APM框架层交互的接口，包括不限于：
* 各个功能开关，阈值设置
* 各个功能的列表设置
* 存储接口
* 按键注入接口

### 6.1 调用API所需的权限

### 如何使用gradle构建项目
[示例项目](https://github.com/Tornaco/X-APM-Base-Sample)

1. 添加依赖

[ ![Download](https://api.bintray.com/packages/potestadetornaco/android/x-apm-base/images/download.svg) ](https://bintray.com/potestadetornaco/android/x-apm-base/_latestVersion)

```
implementation 'github.tornaco:x-apm-base:1.0.1'
```
2. 使用JDK1.8
```
    compileOptions {
        targetCompatibility 1.8
        sourceCompatibility 1.8
    }
```

3. 代码示例
```java
        // 获取X-APM框架层服务是否成功注入
        XAshmanManager ashmanManager = XAshmanManager.get();
        boolean isActivated = ashmanManager.isServiceAvailable();
        sb.append("active: " + isActivated).append("\n");

        // 获取X-APM框架层序列号
        String buildSerial = ashmanManager.getBuildSerial();
        sb.append("buildSerial: " + buildSerial).append("\n");
```
