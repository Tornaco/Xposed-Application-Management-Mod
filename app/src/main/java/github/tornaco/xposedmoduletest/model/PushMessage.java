package github.tornaco.xposedmoduletest.model;

import android.support.annotation.Nullable;

import java.util.UUID;

import github.tornaco.xposedmoduletest.util.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by guohao4 on 2018/1/29.
 * Email: Tornaco@163.com
 */

// Sample:

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {
    public static final int IMPORTANCE_MAX = 0;


    public static final int TYPE_APP_UPDATE = 0x1;


    public static final String DATA_SCHEMA_FIREBASE_BODY = "body";

    private String title;
    private String message;
    private int type;
    private String[] payload;
    private long timeMills;
    private int from;
    private String messageId;
    private int importance;
    private boolean isTest;

    public static PushMessage makeDummy() {
        PushMessage p = new PushMessage();
        p.setImportance(0);
        p.setMessage("Hello world!");
        p.setMessageId(UUID.randomUUID().toString());
        p.setPayload(new String[]{"www.google.com"});
        p.setTest(false);
        p.setTimeMills(System.currentTimeMillis());
        p.setTitle("New message");
        p.setType(0);

        return p;
    }

    public String toJson() {
        return GsonUtil.getGson().toJson(this);
    }

    @Nullable
    public static PushMessage fromJson(String js) {
        try {
            return GsonUtil.getGson().fromJson(js, PushMessage.class);
        } catch (Throwable e) {
            return null;
        }
    }
}
