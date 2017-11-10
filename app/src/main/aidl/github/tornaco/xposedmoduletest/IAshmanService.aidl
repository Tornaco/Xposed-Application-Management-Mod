// IIntentFirewall.aidl
package github.tornaco.xposedmoduletest;

interface IAshmanService {
    void clearProcess();
    void setIFWEnabled(boolean enabled);
    boolean isIFWEnabled();
}
