package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import github.tornaco.xposedmoduletest.IWatcher;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */

class XAppGuardServiceDelegate extends XAppGuardServiceAbs {

    private XAppGuardServiceAbs mImpl;

    XAppGuardServiceDelegate() {
        mImpl = new XAppGuardServiceImplDev();
        XLog.logF("Bring up XAppGuardService using impl:" + mImpl + " of date:" + mImpl.commitDate());
    }

    @Override
    public String commitDate() {
        return mImpl.commitDate();
    }

    @Override
    public void attachContext(Context context) {
        mImpl.attachContext(context);
    }

    @Override
    public void publish() {
        mImpl.publish();
    }

    @Override
    public void systemReady() {
        mImpl.systemReady();
    }

    @Override
    public void publishFeature(String f) {
        mImpl.publishFeature(f);
    }

    @Override
    public void setStatus(XStatus xStatus) {
        mImpl.setStatus(xStatus);
    }

    @Override
    public void shutdown() {
        mImpl.shutdown();
    }

    @Override
    public boolean passed(String pkg) {
        return mImpl.passed(pkg);
    }

    @Override
    public void verify(Bundle options, String pkg, int uid, int pid, XAppGuardServiceImpl.VerifyListener listener) {
        mImpl.verify(options, pkg, uid, pid, listener);
    }

    @Override
    public void onHome() {
        mImpl.onHome();
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        return mImpl.isBlurForPkg(pkg);
    }

    @Override
    public boolean isEnabled() throws RemoteException {
        return mImpl.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException {
        mImpl.setEnabled(enabled);
    }

    @Override
    public void setVerifyOnScreenOff(boolean ver) throws RemoteException {
        mImpl.setVerifyOnScreenOff(ver);
    }

    @Override
    public boolean isVerifyOnScreenOff() throws RemoteException {
        return mImpl.isVerifyOnScreenOff();
    }

    @Override
    public void setVerifyOnHome(boolean ver) throws RemoteException {
        mImpl.setVerifyOnHome(ver);
    }

    @Override
    public boolean isVerifyOnHome() throws RemoteException {
        return mImpl.isVerifyOnHome();
    }

    @Override
    public boolean isBlur() throws RemoteException {
        return mImpl.isBlur();
    }

    @Override
    public void setBlur(boolean blur) throws RemoteException {
        mImpl.setBlur(blur);
    }

    @Override
    public void setBlurPolicy(int policy) throws RemoteException {
        mImpl.setBlurPolicy(policy);
    }

    @Override
    public int getBlurPolicy() throws RemoteException {
        return mImpl.getBlurPolicy();
    }

    @Override
    public void setBlurRadius(int radius) throws RemoteException {
        mImpl.setBlurRadius(radius);
    }

    @Override
    public int getBlurRadius() throws RemoteException {
        return mImpl.getBlurRadius();
    }

    @Override
    public void setBlurScale(float scale) throws RemoteException {
        mImpl.setBlurScale(scale);
    }

    @Override
    public float getBlurScale() throws RemoteException {
        return mImpl.getBlurScale();
    }

    @Override
    public void setAllow3rdVerifier(boolean allow) throws RemoteException {
        mImpl.setAllow3rdVerifier(allow);
    }

    @Override
    public boolean isAllow3rdVerifier() throws RemoteException {
        return mImpl.isAllow3rdVerifier();
    }

    @Override
    public void setPasscode(String passcode) throws RemoteException {
        mImpl.setPasscode(passcode);
    }

    @Override
    public String getPasscode() throws RemoteException {
        return mImpl.getPasscode();
    }

    @Override
    public boolean hasFeature(String feature) throws RemoteException {
        return mImpl.hasFeature(feature);
    }

    @Override
    public void ignore(String pkg) throws RemoteException {
        mImpl.ignore(pkg);
    }

    @Override
    public void pass(String pkg) throws RemoteException {
        mImpl.pass(pkg);
    }

    @Override
    public int getStatus() throws RemoteException {
        return mImpl.getStatus();
    }

    @Override
    public String[] getPackages() throws RemoteException {
        return mImpl.getPackages();
    }

    @Override
    public void setResult(int transactionID, int res) throws RemoteException {
        mImpl.setResult(transactionID, res);
    }

    @Override
    public void testUI() throws RemoteException {
        mImpl.testUI();
    }

    @Override
    public void addPackages(String[] pkgs) throws RemoteException {
        mImpl.addPackages(pkgs);
    }

    @Override
    public void removePackages(String[] pkgs) throws RemoteException {
        mImpl.removePackages(pkgs);
    }

    @Override
    public void watch(IWatcher w) throws RemoteException {
        mImpl.watch(w);
    }

    @Override
    public void unWatch(IWatcher w) throws RemoteException {
        mImpl.unWatch(w);
    }

    @Override
    public void forceWriteState() throws RemoteException {
        mImpl.forceWriteState();
    }

    @Override
    public void forceReadState() throws RemoteException {
        mImpl.forceReadState();
    }

    @Override
    public void mockCrash() throws RemoteException {
        mImpl.mockCrash();
    }
}
