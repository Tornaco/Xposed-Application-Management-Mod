---
layout: post
title:  "应用介绍"
summary: "介绍X-APM开发缘由与原理，数据存储，以及如何卸载"
date:   2018-07-20 15:50:00
categories: jekyll
---

<!-- more -->

## 开发初衷
基于**Xposed**框架的patch特性，可以很方便的用作Android Framework开发调试，
因此一开始**X-APM**仅仅是用来试水**Xposed**框架的一个小小的测试，因此我们可以看到应用的包名是以*test*结尾的，
后来因为改了包名会导致应用需要重新安装，保留了该包名（某个国产ROM，如果包名是*test*结尾，会被认为是测试包，Release版本上无法安装）。

## 功能来源
大部分功能的idea来自华为的**EMUI6**中集成的**手机管家**，也有一部分来自酷友的建议。这些功能都是偏向设备于应用的管理与优化方面，因此美化相关的功能是不存在的。

## 工作原理
**X-APM**整体架构分两层，分别是**APP**层，**FW**（Framework）层。

![Arch-Android-X-APM](/X-APM/assets/post-app-introduce/X-APM-Arch-Android.jpg)

* **FW**层运行于`system_server`进程，伴随着**AMS**（ActivityManagerService，系统中的一个管理服务）的启动而启动，
只要负责核心管理逻辑，拥有至高无上的权限，用户层APP无法干扰。
* **APP**层仅仅是一个普通的应用级别，只负责为用户提供UI交互，可与FW层进行IPC，完成用户的配置。

因此，**X-APM**应用在Android初始化的时候就完成了部署，设备启动完成后，用户无需进行后台的维护，用户的感受就是无需后台。

## 数据存储
由于**X-APM**整体架构分两层，其数据也分两部分存储。

* **FW**层的数据包括各功能开关有**FW**负责处理，存储在`/data/system/tor`下，不root的情况下之能在`system_server`进程中读取，
以此保证FW层核心数据的安全。
* **APP**层的数据主要是UI相关的设定数据，例如主题，图标包的设定。存放在标准的应用数据目录下，如果用户通过系统设置，清除了**X-APM**应用数据，只有APP层数据会被清除。

如果想要卸载**X-APM**模块并清除其所有数据，可以前往**X-APM**的*更多-备份与还原-立即卸载*。
![Uninstall-X-APM](/X-APM/assets/post-app-introduce/Uninstall-X-APM.png)
如果依然无法删除，请在卸载X-APM应用后，手动删除`/data/system/tor`目录。