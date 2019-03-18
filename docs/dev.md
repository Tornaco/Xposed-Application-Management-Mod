---
layout: page
title: 开发
permalink: /dev/
---

## 编译

### 编译环境配置
* 需要配置Android SDK
* 需要配置Gradle

### 克隆代码到本地
```git clone https://github.com/Tornaco/X-APM X-APM```

### 配置需要编译的模块

> 默认编译配置文件路径为：源码根目录```building```，里面定义了此次编译要包含的模块，可以根据需求增加或者删除。

| 变量        | 含义           |
| ------------- |:-------------:|
| app_lock      | 应用锁 |
| app_blur      | 模糊      |
| app_uninstall | 阻止卸载      |
| app_ops | 权限      |
| app_boot | 自启动      |
| app_start | 关联启动      |
| app_green | 绿化      |
| app_lk | 锁屏清理      |
| app_rfk | 返回强退      |
| app_lazy | 乖巧      |
| app_firewall | 防火墙      |
| app_smart_sense | 情景模式      |
| app_privacy | 隐匿      |
| app_comp_edit | 组件控制      |
| app_comp_replace | 移花接木      |
| play | 是否是Play版本      |

### 执行gradle命令进行编译

将你的签名key放在```building/keys```下，执行以下命令编译：

* windows

```./gradlew x-apm-app:assembleRelease```

* linux/mac

```./gradle x-apm-app:assembleRelease```


## 测试

### UI自动化测试
自动化测试基于**Android uiautomator**框架编写，代码位于```x-apm-app/src/androidTest```下（未完成）。
