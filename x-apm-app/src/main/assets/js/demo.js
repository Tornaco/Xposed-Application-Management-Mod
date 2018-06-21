// 打印日志
log("Hello world!");
// 显示土司
toast("Hello world!");

// 点击坐标
tap(100, 200);

// 等待8秒
threadWait(8000);

// 输入文字
input("AABBCC9900");

// 注入按键事件，参考android.view.KeyEvent
keyevent(3);

// Menu键
menu();

threadWait(1000);

// 获取当前所运行的应用包名
var top = getTopPackage();
toast(top);

threadWait(3000);

// 电源键
power();
