var qqmusic = "com.tencent.qqmusic";

function waitForTwoSeconds() {
    threadWait(2000);
}

function comment() {
    killApp(qqmusic);

    toast("启动QQ Music");
    launchApp(qqmusic);
     threadWait(10000);

    toast("音乐馆");
    tap(562, 133);
    waitForTwoSeconds();

    toast("电台");
    tap(167, 1193);
    waitForTwoSeconds();

    toast("详情");
    tap(636, 1832);
    waitForTwoSeconds();

    toast("评论");
    tap(666, 1824);
    waitForTwoSeconds();

    toast("输入框");
    tap(741, 1850);
    waitForTwoSeconds();

    for (var i = 0; i < 1000; i++) {
           toast("输入");
           var date = new java.util.Date();
           input("Sounds good, I like it! ---By X-APM Workflow test@" + date.toString());
           waitForTwoSeconds();

           toast("发送");
           tap(989, 1000);
           waitForTwoSeconds();
    }
}

comment();
