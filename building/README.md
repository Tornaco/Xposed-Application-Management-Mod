# 自选编译

要配置Hidden API 参考连接：https://github.com/anggrayudi/android-hidden-api （本项目使用的是v26版本，代码目录hiddenapi下已经包含，因此你可以看下左边给的配置链接，直接用hiddenapi下的android.jar进行配置）。

### 克隆代码到本地
```git clone https://github.com/Tornaco/X-APM X-APM```

### 配置需要编译的模块

> 默认编译配置文件路径为：源码根目录```building```，里面定义了此次编译要包含的模块，可以根据需求增加或者删除。

具体各个可用模块：

app_lock 应用锁

app_blur 模糊

app_uninstall 阻止卸载

app_ops 权限

app_boot 自启动

app_start 关联启动

app_green 绿化

app_lk 锁屏清理

app_rfk 返回强退

app_lazy 乖巧

app_firewall 防火墙

app_smart_sense 情景模式

app_privacy 隐匿

app_comp_edit 组件控制

app_comp_replace 移花接木

play 是否是Play版本

### 执行gradle命令进行编译

```./gradlew x-apm-app:assembleRelease```
