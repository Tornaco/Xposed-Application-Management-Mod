---
layout: post
title:  "自启限制"
summary: "关于自启限制功能的一切"
date:   2018-07-16 15:58:00
categories: jekyll
---
<!-- more -->

## 用处
正如其名称，为了**禁止**某个应用在开机之后通过注册的**开机广播（Broadcast receiver）**达到启动自身进程（可启动服务，活动等）的目的。

## 如何使用
将需要限制的应用加入**自启限制**列表中即可。

如下图，就禁止了`百度地图`的自启动。
![xposed_installer_main](/X-APM/assets/post-boot-blocker/Boot-Blocker-Sample.png)
