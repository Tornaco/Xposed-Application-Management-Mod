/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.InterestingConfigChanges;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Singleton for retrieving and monitoring the state about all running
 * applications/processes/services.
 */
public class RunningState {

    private static final String TAG = "RunningState";
    private static final boolean DEBUG_COMPARE = false;

    private static Object sGlobalLock = new Object();
    private static RunningState sInstance;

    private static final int MSG_RESET_CONTENTS = 1;
    private static final int MSG_UPDATE_CONTENTS = 2;
    private static final int MSG_REFRESH_UI = 3;
    private static final int MSG_UPDATE_TIME = 4;

    private static final long TIME_UPDATE_DELAY = 60 * 1000;
    private static final long CONTENTS_UPDATE_DELAY = 60 * 2000;

    private static final int MAX_SERVICES = 100;

    private final Context mApplicationContext;
    final PackageManager mPm;

    private final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();

    // Processes that are hosting a service we are interested in, organized
    // by uid and name.  Note that this mapping does not change even across
    // service restarts, and during a restart there will still be a process
    // entry.
    private final SparseArray<HashMap<String, ProcessItem>> mServiceProcessesByName
            = new SparseArray<>();

    // Processes that are hosting a service we are interested in, organized
    // by their pid.  These disappear and re-appear as services are restarted.
    private final SparseArray<ProcessItem> mServiceProcessesByPid
            = new SparseArray<>();

    // Used to sort the interesting processes.
    private final ServiceProcessComparator mServiceProcessComparator
            = new ServiceProcessComparator();

    // Additional interesting processes to be shown to the user, even if
    // there is no service running in them.
    private final ArrayList<ProcessItem> mInterestingProcesses = new ArrayList<>();

    // All currently running processes, for finding dependencies etc.
    private final SparseArray<ProcessItem> mRunningProcesses
            = new SparseArray<>();

    // The processes associated with services, in sorted order.
    private final ArrayList<ProcessItem> mProcessItems = new ArrayList<>();

    // All processes, used for retrieving memory information.
    private final ArrayList<ProcessItem> mAllProcessItems = new ArrayList<>();

    // If there are other users on the device, these are the merged items
    // representing all items that would be put in mMergedItems for that user.
    private final SparseArray<MergedItem> mOtherUserMergedItems = new SparseArray<>();

    static class AppProcessInfo {
        final ActivityManager.RunningAppProcessInfo info;
        boolean hasServices;
        boolean hasForegroundServices;

        AppProcessInfo(ActivityManager.RunningAppProcessInfo _info) {
            info = _info;
        }
    }

    // Temporary structure used when updating above information.
    private final SparseArray<AppProcessInfo> mTmpAppProcesses = new SparseArray<>();

    private int mSequence = 0;

    final Comparator<MergedItem> mBackgroundComparator
            = new Comparator<MergedItem>() {
        @Override
        public int compare(MergedItem lhs, MergedItem rhs) {
            if (DEBUG_COMPARE) {
                Log.i(TAG, "Comparing " + lhs + " with " + rhs);
                Log.i(TAG, "     Proc " + lhs.mProcess + " with " + rhs.mProcess);
            }
            if (lhs.mProcess == rhs.mProcess) {
                if (lhs.mLabel == rhs.mLabel) {
                    return 0;
                }
                return lhs.mLabel != null ? lhs.mLabel.compareTo(rhs.mLabel) : -1;
            }
            if (lhs.mProcess == null) return -1;
            if (rhs.mProcess == null) return 1;
            if (DEBUG_COMPARE) Log.i(TAG, "    Label " + lhs.mProcess.mLabel
                    + " with " + rhs.mProcess.mLabel);
            final ActivityManager.RunningAppProcessInfo lhsInfo
                    = lhs.mProcess.mRunningProcessInfo;
            final ActivityManager.RunningAppProcessInfo rhsInfo
                    = rhs.mProcess.mRunningProcessInfo;
            final boolean lhsBg = lhsInfo.importance
                    >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            final boolean rhsBg = rhsInfo.importance
                    >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            if (DEBUG_COMPARE) Log.i(TAG, "       Bg " + lhsBg + " with " + rhsBg);
            if (lhsBg != rhsBg) {
                return lhsBg ? 1 : -1;
            }
            final boolean lhsA = (lhsInfo.flags
                    & ActivityManager.RunningAppProcessInfo.FLAG_HAS_ACTIVITIES) != 0;
            final boolean rhsA = (rhsInfo.flags
                    & ActivityManager.RunningAppProcessInfo.FLAG_HAS_ACTIVITIES) != 0;
            if (DEBUG_COMPARE) Log.i(TAG, "      Act " + lhsA + " with " + rhsA);
            if (lhsA != rhsA) {
                return lhsA ? -1 : 1;
            }
            if (DEBUG_COMPARE) Log.i(TAG, "      Lru " + lhsInfo.lru + " with " + rhsInfo.lru);
            if (lhsInfo.lru != rhsInfo.lru) {
                return lhsInfo.lru < rhsInfo.lru ? -1 : 1;
            }
            if (lhs.mProcess.mLabel == rhs.mProcess.mLabel) {
                return 0;
            }
            if (lhs.mProcess.mLabel == null) return 1;
            if (rhs.mProcess.mLabel == null) return -1;
            return lhs.mProcess.mLabel.compareTo(rhs.mProcess.mLabel);
        }
    };

    // ----- following protected by mLock -----

    // Lock for protecting the state that will be shared between the
    // background update thread and the UI thread.
    final Object mLock = new Object();

    boolean mResumed;
    boolean mHaveData;
    boolean mWatchingBackgroundItems;

    ArrayList<BaseItem> mItems = new ArrayList<>();
    ArrayList<MergedItem> mMergedItems = new ArrayList<>();
    ArrayList<MergedItem> mBackgroundItems = new ArrayList<>();
    ArrayList<MergedItem> mUserBackgroundItems = new ArrayList<>();

    int mNumBackgroundProcesses;
    long mBackgroundProcessMemory;
    int mNumForegroundProcesses;
    long mForegroundProcessMemory;
    int mNumServiceProcesses;
    long mServiceProcessMemory;

    private final class UserManagerBroadcastReceiver extends BroadcastReceiver {
        private volatile boolean usersChanged;

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (mLock) {
                if (mResumed) {
                    mHaveData = false;
                } else {
                    usersChanged = true;
                }
            }
        }

        public boolean checkUsersChangedLocked() {
            boolean oldValue = usersChanged;
            usersChanged = false;
            return oldValue;
        }

        void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_USER_STOPPED);
            filter.addAction(Intent.ACTION_USER_STARTED);
            filter.addAction(Intent.ACTION_USER_INFO_CHANGED);
            context.registerReceiver(this, filter);
        }
    }

    private final UserManagerBroadcastReceiver mUmBroadcastReceiver =
            new UserManagerBroadcastReceiver();

    // ----- DATA STRUCTURES -----

    static class BaseItem {
        final boolean mIsProcess;

        PackageItemInfo mPackageInfo;
        CharSequence mDisplayLabel;
        String mLabel;
        String mDescription;

        int mCurSeq;

        long mActiveSince;
        long mSize;
        String mSizeStr;
        String mCurSizeStr;
        boolean mNeedDivider;
        boolean mBackground;

        public BaseItem(boolean isProcess) {
            mIsProcess = isProcess;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (mPackageInfo != null) {
                return mPackageInfo.loadIcon(state.mPm);
            }
            return null;
        }
    }

    static class ServiceItem extends BaseItem {
        ActivityManager.RunningServiceInfo mRunningService;
        ServiceInfo mServiceInfo;
        String starter;
        boolean mShownAsStarted;

        MergedItem mMergedItem;

        public ServiceItem() {
            super(false);
        }
    }

    static class ProcessItem extends BaseItem {
        final HashMap<ComponentName, ServiceItem> mServices
                = new HashMap<>();
        final SparseArray<ProcessItem> mDependentProcesses
                = new SparseArray<>();

        final int mUid;
        final String mProcessName;
        int mPid;

        ProcessItem mClient;
        int mLastNumDependentProcesses;

        int mRunningSeq;
        ActivityManager.RunningAppProcessInfo mRunningProcessInfo;

        MergedItem mMergedItem;

        boolean mInteresting;

        // Purely for sorting.
        boolean mIsSystem;
        boolean mIsStarted;
        long mActiveSince;

        public ProcessItem(Context context, int uid, String processName) {
            super(true);
            mDescription = context.getResources().getString(
                    R.string.service_process_name, processName);
            mUid = uid;
            mProcessName = processName;
        }

        void ensureLabel(PackageManager pm) {
            if (mLabel != null) {
                return;
            }

            try {
                ApplicationInfo ai = pm.getApplicationInfo(mProcessName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                if (ai.uid == mUid) {
                    mDisplayLabel = ai.loadLabel(pm);
                    mLabel = mDisplayLabel.toString();
                    mPackageInfo = ai;
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }

            // If we couldn't get information about the overall
            // process, try to find something about the uid.
            String[] pkgs = pm.getPackagesForUid(mUid);

            // If there is one package with this uid, that is what we want.
            if (pkgs.length == 1) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(pkgs[0],
                            PackageManager.GET_UNINSTALLED_PACKAGES);
                    mDisplayLabel = ai.loadLabel(pm);
                    mLabel = mDisplayLabel.toString();
                    mPackageInfo = ai;
                    return;
                } catch (PackageManager.NameNotFoundException e) {
                }
            }

            // If there are multiple, see if one gives us the official name
            // for this uid.
            for (String name : pkgs) {
                try {
                    PackageInfo pi = pm.getPackageInfo(name, 0);
                    if (pi.sharedUserLabel != 0) {
                        CharSequence nm = pm.getText(name,
                                pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            mDisplayLabel = nm;
                            mLabel = nm.toString();
                            mPackageInfo = pi.applicationInfo;
                            return;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }

            // If still don't have anything to display, just use the
            // service info.
            if (mServices.size() > 0) {
                ApplicationInfo ai = mServices.values().iterator().next()
                        .mServiceInfo.applicationInfo;
                mPackageInfo = ai;
                mDisplayLabel = mPackageInfo.loadLabel(pm);
                mLabel = mDisplayLabel.toString();
                return;
            }

            // Finally... whatever, just pick the first package's name.
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkgs[0],
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                mDisplayLabel = ai.loadLabel(pm);
                mLabel = mDisplayLabel.toString();
                mPackageInfo = ai;
                return;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        boolean updateService(Context context, ActivityManager.RunningServiceInfo service) {
            final PackageManager pm = context.getPackageManager();

            boolean changed = false;
            ServiceItem si = mServices.get(service.service);
            if (si == null) {
                changed = true;
                si = new ServiceItem();
                si.mRunningService = service;
                try {
                    si.mServiceInfo = pm.getServiceInfo(service.service, PackageManager.GET_UNINSTALLED_PACKAGES);
                } catch (PackageManager.NameNotFoundException ignored) {

                }
                if (si.mServiceInfo == null) {
                    Log.d("RunningService", "getServiceInfo returned null for: "
                            + service.service);
                    return false;
                }
                // Query starter.
                if (XAPMManager.get().isServiceAvailable()) {
                    // updateNow: java.lang.NoSuchMethodError: No virtual method getComponentName()Landroid/content/ComponentName; in class Landroid/content/pm/
                    // si.starter = XAPMManager.get().getServiceStarter(si.mServiceInfo.getComponentName());
                }
                si.mDisplayLabel = makeLabel(pm,
                        si.mRunningService.service.getClassName(), si.mServiceInfo);
                mLabel = mDisplayLabel != null ? mDisplayLabel.toString() : null;
                si.mPackageInfo = si.mServiceInfo.applicationInfo;
                mServices.put(service.service, si);
            }
            si.mCurSeq = mCurSeq;
            si.mRunningService = service;
            long activeSince = service.restarting == 0 ? service.activeSince : -1;
            if (si.mActiveSince != activeSince) {
                si.mActiveSince = activeSince;
                changed = true;
            }
            if (service.clientPackage != null && service.clientLabel != 0) {
                if (si.mShownAsStarted) {
                    si.mShownAsStarted = false;
                    changed = true;
                }
                try {
                    Resources clientr = pm.getResourcesForApplication(service.clientPackage);
                    String label = clientr.getString(service.clientLabel);
                    si.mDescription = context.getResources().getString(
                            R.string.service_client_name, label);
                } catch (PackageManager.NameNotFoundException e) {
                    si.mDescription = null;
                }
            } else {
                if (!si.mShownAsStarted) {
                    si.mShownAsStarted = true;
                    changed = true;
                }
                si.mDescription = context.getResources().getString(
                        R.string.service_started_by_app);
            }

            return changed;
        }

        boolean updateSize(Context context, long pss, int curSeq) {
            mSize = pss * 1024;
            if (mCurSeq == curSeq) {
                String sizeStr = Formatter.formatShortFileSize(
                        context, mSize);
                if (!sizeStr.equals(mSizeStr)) {
                    mSizeStr = sizeStr;
                    // We update this on the second tick where we update just
                    // the text in the current items, so no need to say we
                    // changed here.
                    return false;
                }
            }
            return false;
        }

        boolean buildDependencyChain(Context context, PackageManager pm, int curSeq) {
            final int NP = mDependentProcesses.size();
            boolean changed = false;
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = mDependentProcesses.valueAt(i);
                if (proc.mClient != this) {
                    changed = true;
                    proc.mClient = this;
                }
                proc.mCurSeq = curSeq;
                proc.ensureLabel(pm);
                changed |= proc.buildDependencyChain(context, pm, curSeq);
            }

            if (mLastNumDependentProcesses != mDependentProcesses.size()) {
                changed = true;
                mLastNumDependentProcesses = mDependentProcesses.size();
            }

            return changed;
        }

        void addDependentProcesses(ArrayList<BaseItem> dest,
                                   ArrayList<ProcessItem> destProc) {
            final int NP = mDependentProcesses.size();
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = mDependentProcesses.valueAt(i);
                proc.addDependentProcesses(dest, destProc);
                dest.add(proc);
                if (proc.mPid > 0) {
                    destProc.add(proc);
                }
            }
        }
    }

    public static class MergedItem extends BaseItem {
        ProcessItem mProcess;
        final ArrayList<ProcessItem> mOtherProcesses = new ArrayList<>();
        final ArrayList<ServiceItem> mServices = new ArrayList<>();
        final ArrayList<MergedItem> mChildren = new ArrayList<>();

        private int mLastNumProcesses = -1, mLastNumServices = -1;

        MergedItem() {
            super(false);
        }

        private void setDescription(Context context, int numProcesses, int numServices) {
            if (mLastNumProcesses != numProcesses || mLastNumServices != numServices) {
                mLastNumProcesses = numProcesses;
                mLastNumServices = numServices;
                int resid = R.string.running_processes_item_description_s_s;
                if (numProcesses != 1) {
                    resid = numServices != 1
                            ? R.string.running_processes_item_description_p_p
                            : R.string.running_processes_item_description_p_s;
                } else if (numServices != 1) {
                    resid = R.string.running_processes_item_description_s_p;
                }
                mDescription = context.getResources().getString(resid, numProcesses,
                        numServices);
            }
        }

        boolean update(Context context, boolean background) {
            mBackground = background;

            mPackageInfo = mProcess.mPackageInfo;
            mDisplayLabel = mProcess.mDisplayLabel;
            mLabel = mProcess.mLabel;

            if (!mBackground) {
                setDescription(context, (mProcess.mPid > 0 ? 1 : 0) + mOtherProcesses.size(),
                        mServices.size());
            }

            mActiveSince = -1;
            for (int i = 0; i < mServices.size(); i++) {
                ServiceItem si = mServices.get(i);
                if (si.mActiveSince >= 0 && mActiveSince < si.mActiveSince) {
                    mActiveSince = si.mActiveSince;
                }
            }

            return false;
        }

        boolean updateSize(Context context) {
            mSize = mProcess.mSize;
            for (int i = 0; i < mOtherProcesses.size(); i++) {
                mSize += mOtherProcesses.get(i).mSize;
            }

            String sizeStr = Formatter.formatShortFileSize(
                    context, mSize);
            if (!sizeStr.equals(mSizeStr)) {
                mSizeStr = sizeStr;
                // We update this on the second tick where we update just
                // the text in the current items, so no need to say we
                // changed here.
                return false;
            }
            return false;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            return context.getDrawable(
                    com.android.internal.R.drawable.ic_menu_cc);
        }

        public int serviceCount() {
            return mServices == null ? 0 : mServices.size();
        }
    }

    class ServiceProcessComparator implements Comparator<ProcessItem> {
        public int compare(ProcessItem object1, ProcessItem object2) {
            if (object1.mIsStarted != object2.mIsStarted) {
                // Non-started processes go last.
                return object1.mIsStarted ? -1 : 1;
            }
            if (object1.mIsSystem != object2.mIsSystem) {
                // System processes go below non-system.
                return object1.mIsSystem ? 1 : -1;
            }
            if (object1.mActiveSince != object2.mActiveSince) {
                // Remaining ones are sorted with the longest running
                // services last.
                return (object1.mActiveSince > object2.mActiveSince) ? -1 : 1;
            }
            return 0;
        }
    }

    static CharSequence makeLabel(PackageManager pm,
                                  String className, PackageItemInfo item) {
        if (item != null && (item.labelRes != 0
                || item.nonLocalizedLabel != null)) {
            CharSequence label = item.loadLabel(pm);
            if (label != null) {
                return label;
            }
        }

        String label = className;
        int tail = label.lastIndexOf('.');
        if (tail >= 0) {
            label = label.substring(tail + 1, label.length());
        }
        return label;
    }

    public static RunningState getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new RunningState(context);
            }
            return sInstance;
        }
    }

    private RunningState(Context context) {
        mApplicationContext = context.getApplicationContext();
        mPm = mApplicationContext.getPackageManager();
        mResumed = false;
        mUmBroadcastReceiver.register(mApplicationContext);
    }

    void resume() {
        synchronized (mLock) {
            mResumed = true;
            boolean usersChanged = mUmBroadcastReceiver.checkUsersChangedLocked();
            boolean configChanged =
                    mInterestingConfigChanges.applyNewConfig(mApplicationContext.getResources());
            if (usersChanged || configChanged) {
                mHaveData = false;
            }
        }
    }

    public void updateNow() {
        synchronized (mLock) {
            try {
                update(mApplicationContext);
            } catch (Throwable e) {
                Logger.e("updateNow: " + Logger.getStackTraceString(e));
            }
        }
    }

    boolean hasData() {
        synchronized (mLock) {
            return mHaveData;
        }
    }

    void waitForData() {
        synchronized (mLock) {
            while (!mHaveData) {
                try {
                    mLock.wait(0);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    void pause() {
        synchronized (mLock) {
            mResumed = false;
        }
    }

    private boolean isInterestingProcess(ActivityManager.RunningAppProcessInfo pi) {
        if ((pi.flags & ActivityManager.RunningAppProcessInfo.FLAG_CANT_SAVE_STATE) != 0) {
            return true;
        }
        if ((pi.flags & ActivityManager.RunningAppProcessInfo.FLAG_PERSISTENT) == 0
                && pi.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && pi.importance < ActivityManager.RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE
                && pi.importanceReasonCode
                == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
            return true;
        }
        return false;
    }

    void reset() {
        mServiceProcessesByName.clear();
        mServiceProcessesByPid.clear();
        mInterestingProcesses.clear();
        mRunningProcesses.clear();
        mProcessItems.clear();
        mAllProcessItems.clear();
    }

    private boolean update(Context context) {
        Logger.d("update: " + Thread.currentThread().getName());
        final PackageManager pm = context.getPackageManager();

        mSequence++;

        boolean changed = false;

        // Retrieve list of services, filtering out anything that definitely
        // won't be shown in the UI.
        List<ActivityManager.RunningServiceInfo> services
                = XAPMManager.get().getRunningServices(MAX_SERVICES);
        int NS = services != null ? services.size() : 0;
        for (int i = 0; i < NS; i++) {
            ActivityManager.RunningServiceInfo si = services.get(i);
            // We are not interested in services that have not been started
            // and don't have a known client, because
            // there is nothing the user can do about them.
            if (!si.started && si.clientLabel == 0) {
                services.remove(i);
                i--;
                NS--;
                continue;
            }
            // We likewise don't care about services running in a
            // persistent process like the system or phone.
            if ((si.flags & ActivityManager.RunningServiceInfo.FLAG_PERSISTENT_PROCESS)
                    != 0) {
                services.remove(i);
                i--;
                NS--;
                continue;
            }
        }

        // Retrieve list of running processes, organizing them into a sparse
        // array for easy retrieval.
        List<ActivityManager.RunningAppProcessInfo> processes
                = XAPMManager.get().getRunningAppProcesses();
        final int NP = processes != null ? processes.size() : 0;
        mTmpAppProcesses.clear();
        for (int i = 0; i < NP; i++) {
            ActivityManager.RunningAppProcessInfo pi = processes.get(i);
            mTmpAppProcesses.put(pi.pid, new AppProcessInfo(pi));
        }

        // Initial iteration through running services to collect per-process
        // info about them.
        for (int i = 0; i < NS; i++) {
            ActivityManager.RunningServiceInfo si = services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                AppProcessInfo ainfo = mTmpAppProcesses.get(si.pid);
                if (ainfo != null) {
                    ainfo.hasServices = true;
                    if (si.foreground) {
                        ainfo.hasForegroundServices = true;
                    }
                }
            }
        }

        // Update state we are maintaining about process that are running services.
        for (int i = 0; i < NS; i++) {
            ActivityManager.RunningServiceInfo si = services.get(i);

            // If this service's process is in use at a higher importance
            // due to another process bound to one of its services, then we
            // won't put it in the top-level list of services.  Instead we
            // want it to be included in the set of processes that the other
            // process needs.
            if (si.restarting == 0 && si.pid > 0) {
                AppProcessInfo ainfo = mTmpAppProcesses.get(si.pid);
                if (ainfo != null && !ainfo.hasForegroundServices) {
                    // This process does not have any foreground services.
                    // If its importance is greater than the service importance
                    // then there is something else more significant that is
                    // keeping it around that it should possibly be included as
                    // a part of instead of being shown by itself.
                    if (ainfo.info.importance
                            < ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                        // Follow process chain to see if there is something
                        // else that could be shown
                        boolean skip = false;
                        ainfo = mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                        while (ainfo != null) {
                            if (ainfo.hasServices || isInterestingProcess(ainfo.info)) {
                                skip = true;
                                break;
                            }
                            ainfo = mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                        }
                        if (skip) {
                            continue;
                        }
                    }
                }
            }

            HashMap<String, ProcessItem> procs = mServiceProcessesByName.get(si.uid);
            if (procs == null) {
                procs = new HashMap<>();
                mServiceProcessesByName.put(si.uid, procs);
            }
            ProcessItem proc = procs.get(si.process);
            if (proc == null) {
                changed = true;
                proc = new ProcessItem(context, si.uid, si.process);
                procs.put(si.process, proc);
            }

            if (proc.mCurSeq != mSequence) {
                int pid = si.restarting == 0 ? si.pid : 0;
                if (pid != proc.mPid) {
                    changed = true;
                    if (proc.mPid != pid) {
                        if (proc.mPid != 0) {
                            mServiceProcessesByPid.remove(proc.mPid);
                        }
                        if (pid != 0) {
                            mServiceProcessesByPid.put(pid, proc);
                        }
                        proc.mPid = pid;
                    }
                }
                proc.mDependentProcesses.clear();
                proc.mCurSeq = mSequence;
            }
            changed |= proc.updateService(context, si);
        }

        // Now update the map of other processes that are running (but
        // don't have services actively running inside them).
        for (int i = 0; i < NP; i++) {
            ActivityManager.RunningAppProcessInfo pi = processes.get(i);
            ProcessItem proc = mServiceProcessesByPid.get(pi.pid);
            if (proc == null) {
                // This process is not one that is a direct container
                // of a service, so look for it in the secondary
                // running list.
                proc = mRunningProcesses.get(pi.pid);
                if (proc == null) {
                    changed = true;
                    proc = new ProcessItem(context, pi.uid, pi.processName);
                    proc.mPid = pi.pid;
                    mRunningProcesses.put(pi.pid, proc);
                }
                proc.mDependentProcesses.clear();
            }

            if (isInterestingProcess(pi)) {
                if (!mInterestingProcesses.contains(proc)) {
                    changed = true;
                    mInterestingProcesses.add(proc);
                }
                proc.mCurSeq = mSequence;
                proc.mInteresting = true;
                proc.ensureLabel(pm);
            } else {
                proc.mInteresting = false;
            }

            proc.mRunningSeq = mSequence;
            proc.mRunningProcessInfo = pi;
        }

        // Build the chains from client processes to the process they are
        // dependent on; also remove any old running processes.
        int NRP = mRunningProcesses.size();
        for (int i = 0; i < NRP; ) {
            ProcessItem proc = mRunningProcesses.valueAt(i);
            if (proc.mRunningSeq == mSequence) {
                int clientPid = proc.mRunningProcessInfo.importanceReasonPid;
                if (clientPid != 0) {
                    ProcessItem client = mServiceProcessesByPid.get(clientPid);
                    if (client == null) {
                        client = mRunningProcesses.get(clientPid);
                    }
                    if (client != null) {
                        client.mDependentProcesses.put(proc.mPid, proc);
                    }
                } else {
                    // In this pass the process doesn't have a client.
                    // Clear to make sure that, if it later gets the same one,
                    // we will detect the change.
                    proc.mClient = null;
                }
                i++;
            } else {
                changed = true;
                mRunningProcesses.remove(mRunningProcesses.keyAt(i));
                NRP--;
            }
        }

        // Remove any old interesting processes.
        int NHP = mInterestingProcesses.size();
        for (int i = 0; i < NHP; i++) {
            ProcessItem proc = mInterestingProcesses.get(i);
            if (!proc.mInteresting || mRunningProcesses.get(proc.mPid) == null) {
                changed = true;
                mInterestingProcesses.remove(i);
                i--;
                NHP--;
            }
        }

        // Follow the tree from all primary service processes to all
        // processes they are dependent on, marking these processes as
        // still being active and determining if anything has changed.
        final int NAP = mServiceProcessesByPid.size();
        for (int i = 0; i < NAP; i++) {
            ProcessItem proc = mServiceProcessesByPid.valueAt(i);
            if (proc.mCurSeq == mSequence) {
                changed |= proc.buildDependencyChain(context, pm, mSequence);
            }
        }

        // Look for services and their primary processes that no longer exist...
        ArrayList<Integer> uidToDelete = null;
        for (int i = 0; i < mServiceProcessesByName.size(); i++) {
            HashMap<String, ProcessItem> procs = mServiceProcessesByName.valueAt(i);
            Iterator<ProcessItem> pit = procs.values().iterator();
            while (pit.hasNext()) {
                ProcessItem pi = pit.next();
                if (pi.mCurSeq == mSequence) {
                    pi.ensureLabel(pm);
                    if (pi.mPid == 0) {
                        // Sanity: a non-process can't be dependent on
                        // anything.
                        pi.mDependentProcesses.clear();
                    }
                } else {
                    changed = true;
                    pit.remove();
                    if (procs.size() == 0) {
                        if (uidToDelete == null) {
                            uidToDelete = new ArrayList<>();
                        }
                        uidToDelete.add(mServiceProcessesByName.keyAt(i));
                    }
                    if (pi.mPid != 0) {
                        mServiceProcessesByPid.remove(pi.mPid);
                    }
                    continue;
                }
                Iterator<ServiceItem> sit = pi.mServices.values().iterator();
                while (sit.hasNext()) {
                    ServiceItem si = sit.next();
                    if (si.mCurSeq != mSequence) {
                        changed = true;
                        sit.remove();
                    }
                }
            }
        }

        if (uidToDelete != null) {
            for (int i = 0; i < uidToDelete.size(); i++) {
                int uid = uidToDelete.get(i);
                mServiceProcessesByName.remove(uid);
            }
        }

        if (changed) {
            // First determine an order for the services.
            ArrayList<ProcessItem> sortedProcesses = new ArrayList<>();
            for (int i = 0; i < mServiceProcessesByName.size(); i++) {
                for (ProcessItem pi : mServiceProcessesByName.valueAt(i).values()) {
                    pi.mIsSystem = false;
                    pi.mIsStarted = true;
                    pi.mActiveSince = Long.MAX_VALUE;
                    for (ServiceItem si : pi.mServices.values()) {
                        if (si.mServiceInfo != null
                                && (si.mServiceInfo.applicationInfo.flags
                                & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            pi.mIsSystem = true;
                        }
                        if (si.mRunningService != null
                                && si.mRunningService.clientLabel != 0) {
                            pi.mIsStarted = false;
                            if (pi.mActiveSince > si.mRunningService.activeSince) {
                                pi.mActiveSince = si.mRunningService.activeSince;
                            }
                        }
                    }
                    sortedProcesses.add(pi);
                }
            }

            Collections.sort(sortedProcesses, mServiceProcessComparator);

            ArrayList<BaseItem> newItems = new ArrayList<>();
            ArrayList<MergedItem> newMergedItems = new ArrayList<>();
            mProcessItems.clear();
            for (int i = 0; i < sortedProcesses.size(); i++) {
                ProcessItem pi = sortedProcesses.get(i);
                pi.mNeedDivider = false;

                int firstProc = mProcessItems.size();
                // First add processes we are dependent on.
                pi.addDependentProcesses(newItems, mProcessItems);
                // And add the process itself.
                newItems.add(pi);
                if (pi.mPid > 0) {
                    mProcessItems.add(pi);
                }

                // Now add the services running in it.
                MergedItem mergedItem = null;
                boolean haveAllMerged = false;
                boolean needDivider = false;
                for (ServiceItem si : pi.mServices.values()) {
                    si.mNeedDivider = needDivider;
                    needDivider = true;
                    newItems.add(si);
                    if (si.mMergedItem != null) {
                        if (mergedItem != null && mergedItem != si.mMergedItem) {
                            haveAllMerged = false;
                        }
                        mergedItem = si.mMergedItem;
                    } else {
                        haveAllMerged = false;
                    }
                }

                if (!haveAllMerged || mergedItem == null
                        || mergedItem.mServices.size() != pi.mServices.size()) {
                    // Whoops, we need to build a new MergedItem!
                    mergedItem = new MergedItem();
                    for (ServiceItem si : pi.mServices.values()) {
                        mergedItem.mServices.add(si);
                        si.mMergedItem = mergedItem;
                    }
                    mergedItem.mProcess = pi;
                    mergedItem.mOtherProcesses.clear();
                    for (int mpi = firstProc; mpi < (mProcessItems.size() - 1); mpi++) {
                        mergedItem.mOtherProcesses.add(mProcessItems.get(mpi));
                    }
                }

                mergedItem.update(context, false);
                newMergedItems.add(mergedItem);
            }

            // Finally, interesting processes need to be shown and will
            // go at the top.
            NHP = mInterestingProcesses.size();
            for (int i = 0; i < NHP; i++) {
                ProcessItem proc = mInterestingProcesses.get(i);
                if (proc.mClient == null && proc.mServices.size() <= 0) {
                    if (proc.mMergedItem == null) {
                        proc.mMergedItem = new MergedItem();
                        proc.mMergedItem.mProcess = proc;
                    }
                    proc.mMergedItem.update(context, false);
                    newMergedItems.add(0, proc.mMergedItem);
                    mProcessItems.add(proc);
                }
            }

            // Finally finally, user aggregated merged items need to be
            // updated now that they have all of their children.
            final int NU = mOtherUserMergedItems.size();
            for (int i = 0; i < NU; i++) {
                MergedItem user = mOtherUserMergedItems.valueAt(i);
                if (user.mCurSeq == mSequence) {
                    user.update(context, false);
                }
            }

            synchronized (mLock) {
                mItems = newItems;
                mMergedItems = newMergedItems;
            }
        }

        // Count number of interesting other (non-active) processes, and
        // build a list of all processes we will retrieve memory for.
        mAllProcessItems.clear();
        mAllProcessItems.addAll(mProcessItems);
        int numBackgroundProcesses = 0;
        int numForegroundProcesses = 0;
        int numServiceProcesses = 0;
        NRP = mRunningProcesses.size();
        for (int i = 0; i < NRP; i++) {
            ProcessItem proc = mRunningProcesses.valueAt(i);
            if (proc.mCurSeq != mSequence) {
                // We didn't hit this process as a dependency on one
                // of our active ones, so add it up if needed.
                if (proc.mRunningProcessInfo.importance >=
                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    numBackgroundProcesses++;
                    mAllProcessItems.add(proc);
                } else if (proc.mRunningProcessInfo.importance <=
                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    numForegroundProcesses++;
                    mAllProcessItems.add(proc);
                } else {
                    Log.i("RunningState", "Unknown non-service process: "
                            + proc.mProcessName + " #" + proc.mPid);
                }
            } else {
                numServiceProcesses++;
            }
        }

        long backgroundProcessMemory = 0;
        long foregroundProcessMemory = 0;
        long serviceProcessMemory = 0;
        ArrayList<MergedItem> newBackgroundItems = null;
        ArrayList<MergedItem> newUserBackgroundItems = null;
        final int numProc = mAllProcessItems.size();
        int[] pids = new int[numProc];
        for (int i = 0; i < numProc; i++) {
            pids[i] = mAllProcessItems.get(i).mPid;
        }
        long[] pss = XAPMManager.get()
                .getProcessPss(pids);
        int bgIndex = 0;

        for (int i = 0; i < pids.length; i++) {
            ProcessItem proc = mAllProcessItems.get(i);
            changed |= proc.updateSize(context, pss[i], mSequence);
            if (proc.mCurSeq == mSequence) {
                serviceProcessMemory += proc.mSize;
            } else if (proc.mRunningProcessInfo.importance >=
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                backgroundProcessMemory += proc.mSize;
                MergedItem mergedItem;
                if (newBackgroundItems != null) {
                    mergedItem = proc.mMergedItem = new MergedItem();
                    proc.mMergedItem.mProcess = proc;
                    newBackgroundItems.add(mergedItem);
                } else {
                    if (bgIndex >= mBackgroundItems.size()
                            || mBackgroundItems.get(bgIndex).mProcess != proc) {
                        newBackgroundItems = new ArrayList<>(numBackgroundProcesses);
                        for (int bgi = 0; bgi < bgIndex; bgi++) {
                            mergedItem = mBackgroundItems.get(bgi);
                            newBackgroundItems.add(mergedItem);
                        }
                        mergedItem = proc.mMergedItem = new MergedItem();
                        proc.mMergedItem.mProcess = proc;
                        newBackgroundItems.add(mergedItem);
                    } else {
                        mergedItem = mBackgroundItems.get(bgIndex);
                    }
                }
                mergedItem.update(context, true);
                mergedItem.updateSize(context);
                bgIndex++;
            } else if (proc.mRunningProcessInfo.importance <=
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                foregroundProcessMemory += proc.mSize;
            }
        }

        if (newBackgroundItems == null) {
            // One or more at the bottom may no longer exist.
            if (mBackgroundItems.size() > numBackgroundProcesses) {
                newBackgroundItems = new ArrayList<>(numBackgroundProcesses);
                for (int bgi = 0; bgi < numBackgroundProcesses; bgi++) {
                    MergedItem mergedItem = mBackgroundItems.get(bgi);
                    newBackgroundItems.add(mergedItem);
                }
            }
        }

        if (newBackgroundItems != null) {
            // The background items have changed; we need to re-build the
            // per-user items.
            newUserBackgroundItems = newBackgroundItems;
        }

        for (int i = 0; i < mMergedItems.size(); i++) {
            mMergedItems.get(i).updateSize(context);
        }

        synchronized (mLock) {
            mNumBackgroundProcesses = numBackgroundProcesses;
            mNumForegroundProcesses = numForegroundProcesses;
            mNumServiceProcesses = numServiceProcesses;
            mBackgroundProcessMemory = backgroundProcessMemory;
            mForegroundProcessMemory = foregroundProcessMemory;
            mServiceProcessMemory = serviceProcessMemory;
            if (newBackgroundItems != null) {
                mBackgroundItems = newBackgroundItems;
                mUserBackgroundItems = newUserBackgroundItems;
                if (mWatchingBackgroundItems) {
                    changed = true;
                }
            }
            if (!mHaveData) {
                mHaveData = true;
                mLock.notifyAll();
            }
        }

        return changed;
    }

    public void setWatchingBackgroundItems(boolean watching) {
        synchronized (mLock) {
            mWatchingBackgroundItems = watching;
        }
    }

    public ArrayList<MergedItem> getCurrentMergedItems() {
        synchronized (mLock) {
            return mMergedItems;
        }
    }

    public ArrayList<MergedItem> getCurrentBackgroundItems() {
        synchronized (mLock) {
            return mUserBackgroundItems;
        }
    }
}
