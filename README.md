# ~~作为一个练手项目 2018-02-11，已经可以满足自用需求，由于太耽误时间，永久停止代码和文档维护。~~ 
> 会尽力解决github上提交的Issues，结合自身使用需求进行维护。

# Play版本

<a href="https://play.google.com/store/apps/details?id=github.tornaco.xposedmoduletest"><img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="48"></a>

# 开发与交流TG群组

https://t.me/xposed_apm_mod

# WIKI

https://github.com/Tornaco/X-APM/wiki

# TRAVIS

[![Build Status](https://travis-ci.org/Tornaco/X-APM.svg?branch=master)](https://travis-ci.org/Tornaco/X-APM)

# 介绍

## 1. 目的
> 借助XPOSED实现系统级别应用管理。

### 1.1 功能

**应用锁**

* 应用启动验证。
* 最近任务验证。
* 最近任务高斯模糊（算法来自fastblur）。
* 自定义的验证器。
* 闯入抓拍。
* 指纹验证。

**自启动管理**

* 限制某些（用户选择）应用的开机广播接收。

**管理启动管理**

* 限制某些（用户选择）应用的关联启动（服务/广播）。

**锁屏清理**

* 屏幕锁定后清理后台应用（用户可设置白名单）。

**应用组件控制（开发中）**

* 禁用某应用的某些（用户选择）组件（服务/广播）。

### 已知问题
1. 8.x通知权限。
2. 8.x指纹震动。

## 2. 功能演示
参考README最下方demo视频。

## 3. 设计思路
系统服务Hook与Binder服务注入。

核心服务：
https://github.com/Tornaco/X-APM/blob/master/app/src/main/java/github/tornaco/xposedmoduletest/xposed/service/XAshmanServiceImpl.java

Xposed模块代理：
https://github.com/Tornaco/X-APM/blob/master/app/src/main/java/github/tornaco/xposedmoduletest/xposed/XModuleDelegate.java

## 4. 编译
依赖hiddenapi，Xposed-Framework。

## 4.1 自选编译
https://github.com/Tornaco/X-APM/tree/master/build_var_controls

## 5. 测试
5.1 [查看最新测试报告](TestResults-XAppGuardManagerTest.html)

5.2 测试代码位于```androidTest```目录下。
