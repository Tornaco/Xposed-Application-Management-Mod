package github.tornaco.xposedmoduletest.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */
@Builder
@Getter
public class Contribution {
    private String nick;
    private String ad;
    private String date;
}
