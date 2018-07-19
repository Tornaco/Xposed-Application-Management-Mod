---
layout: post
title:  "安装与激活"
date:   2018-07-19 15:58:00
categories: jekyll
---

## 安装
1. **X-APM**是一个独立的apk，只需要下载apk安装到设备上即可。
2. 目前仅支持**Android 5.0及其以上**版本的ROM。
3. 开发基于**AOSP**开发，理论上兼容所有类原生以及其他大部分国产ROM。

## 激活
1. **X-APM**依赖与**Xposed框架**，因此你的设备必须已经正确安装了**Xposed框架**（包括System-less版本）。
2. 安装**X-APM** apk后，进入**Xposed installer**应用，依次进行如下操作，勾选**X-APM**模块：

![xposed_installer_main](/assets/post-install-activate/xposed_installer_main.png)
![xposed_installer_reboot](/assets/post-install-activate/xposed_installer_reboot.png)

3. 勾选完成，重启设备。