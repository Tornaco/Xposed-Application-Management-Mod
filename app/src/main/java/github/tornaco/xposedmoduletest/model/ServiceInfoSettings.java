package github.tornaco.xposedmoduletest.model;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */
@Getter
@Setter
@NoArgsConstructor
public class ServiceInfoSettings implements Searchable {

    private ServiceInfo serviceInfo;

    private String serviceLabel;
    private String displayName;

    private boolean allowed;

    @AllArgsConstructor
    @Getter
    public static class Export {
        private boolean allowed;
        private ComponentName componentName;
    }

    public boolean mayBeAdComponent() {
        return serviceLabel.contains("AD")
                || serviceLabel.contains("Ad");
    }

    public String simpleName() {
        String simpleName = getDisplayName();
        if (simpleName == null) {
            simpleName = getServiceInfo().packageName;
        }
        final int dot = simpleName.lastIndexOf(".");
        if (dot > 0) {
            return simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
        }
        return simpleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInfoSettings that = (ServiceInfoSettings) o;

        if (!serviceLabel.equals(that.serviceLabel)) return false;
        return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
    }

    @Override
    public int hashCode() {
        int result = serviceLabel.hashCode();
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return simpleName();
    }
}
