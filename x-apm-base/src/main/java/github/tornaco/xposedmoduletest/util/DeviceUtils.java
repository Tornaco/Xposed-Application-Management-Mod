package github.tornaco.xposedmoduletest.util;

public class DeviceUtils {
    private DeviceUtils() {
        // Noop.
    }

    public static boolean isFastDevice() {
        return Runtime.getRuntime().availableProcessors() >= 24;
    }
}
