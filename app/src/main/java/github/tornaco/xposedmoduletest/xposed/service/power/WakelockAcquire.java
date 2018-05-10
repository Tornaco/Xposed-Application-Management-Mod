package github.tornaco.xposedmoduletest.xposed.service.power;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/5/10 12:56.
 * God bless no bug!
 */
@AllArgsConstructor
@Getter
@ToString
public class WakelockAcquire {
    private String tag, packageName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WakelockAcquire that = (WakelockAcquire) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, packageName);
    }
}
