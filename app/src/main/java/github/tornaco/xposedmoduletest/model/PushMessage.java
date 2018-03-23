package github.tornaco.xposedmoduletest.model;

import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import java.util.UUID;

import github.tornaco.xposedmoduletest.util.GsonUtil;
import lombok.Data;

/**
 * Created by guohao4 on 2018/1/29.
 * Email: Tornaco@163.com
 */

// Sample:

@Data
public class PushMessage {
    public static final String DATA_SCHEMA_FIREBASE_BODY = "body";

    private String title;
    private String message;
    private int type;
    private String[] payload;
    private long timeMills;
    private String messageId;
    private int importance;
    private boolean isTest;

    public static void dumpDemo() {
        PushMessage p = new PushMessage();
        p.setImportance(0);
        p.setMessage("Hello world!");
        p.setMessageId(UUID.randomUUID().toString());
        p.setPayload(new String[]{"www.google.com"});
        p.setTest(false);
        p.setTimeMills(System.currentTimeMillis());
        p.setTitle("New message");
        p.setType(0);

        Logger.d("PushMessage demo: " + p);
        Logger.d(p.toJson());
    }

    public String toJson() {
        return GsonUtil.getGson().toJson(this);
    }

    @Nullable
    public static PushMessage fromJson(String js) {
        try {
            return GsonUtil.getGson().fromJson(js, PushMessage.class);
        } catch (Throwable e) {
            Logger.e("Fail fromJson: " + e);
            return null;
        }
    }
}
