## 简介
利用Xposed框架实现的类似于手机管家的Android系统工具。

## 截图
![flow1](art/INTRODUCE_SIMPLE.jpg)

## Rleases

<a href="https://play.google.com/store/apps/details?id=github.tornaco.xposedmoduletest"><img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="48"></a>

<a href="https://www.coolapk.com/apk/github.tornaco.xposedmoduletest"><img src="http://image.coolapk.com/apk_logo/2018/0116/ic_launcher-4599-o_1c3v0ii87s26r0benla7q118eq-uid-97100@192x192.png" height="48"></a>

## 开发与交流

[TG群组](https://t.me/xposed_apm_mod)

## 使用教程

[Wiki中文](https://github.com/Tornaco/X-APM/wiki)

## 编译状态

[![Build Status](https://travis-ci.org/Tornaco/X-APM.svg?branch=master)](https://travis-ci.org/Tornaco/X-APM)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a78b1d40f139418e9c6ce070986ca7e2)](https://www.codacy.com/app/Tornaco/X-APM?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Tornaco/X-APM&amp;utm_campaign=Badge_Grade)

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
* ...

更多请参考[示例项目](https://github.com/Tornaco/X-APM-Base-Sample)
