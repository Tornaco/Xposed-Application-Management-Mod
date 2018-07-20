---
layout: page
title: 下载
permalink: /feedback/
---

## 日志获取
日志是分析bug最重要的环节，请按照以下操作复现问题，截取日志。

默认状态下，为了节省资源，应用管理日志输出处于关闭状态，你需要按照下述步骤，激活日志输出，并且复现你发现的bug。

1. 进入更多-开发者工具-打开调试模式。
2. 复现你的bug。
3. 进入Xposed installer，保存当前日志。
4. 抓取由应用管理采集到的系统错误日志，日志路径```/data/system/tor/trace/```
5. 如果条件允许，请同时使用adb工具，依次执行如下命令：

```adb logcat -vtime > log_cat.log```

```adb pull data/anr/```

```adb logcat -b radio > radio.log```

```adb shell dmesg -c > dmesg.log```

将生成的log文件打包。


## 使用github issue上报
推荐该方式进行上报，会被以最高优先级对待。

请注意提供详细描述，以及相应日志。

[立刻汇报](https://github.com/Tornaco/X-APM/issues/new?template=bug_report.md)

## Email
tornaco@163.com

请注意提供详细描述，以及相应日志。

## Bug汇报完毕
请耐心等待。
