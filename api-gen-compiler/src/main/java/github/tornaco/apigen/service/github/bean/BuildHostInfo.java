package github.tornaco.apigen.service.github.bean;

import java.util.Date;

/**
 * Created by guohao4 on 2017/10/28.
 * Email: Tornaco@163.com
 */

public class BuildHostInfo {
    private String hostName;
    private String date;

    public BuildHostInfo(String hostName, String date) {
        this.hostName = hostName;
        this.date = date;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "BuildHostInfo{" +
                "hostName='" + hostName + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
