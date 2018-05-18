package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.shahroz.svlibrary.interfaces.onSimpleSearchActionsListener;
import com.shahroz.svlibrary.widgets.SearchViewResults;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.util.ColorUtil;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.loader.PaletteColorPicker;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettings;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettingsList;
import github.tornaco.xposedmoduletest.model.Searchable;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettingsList;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.WithSearchActivity;
import github.tornaco.xposedmoduletest.ui.adapter.ActivitySettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ReceiverSettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ServiceSettingsAdapter;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;

@RuntimePermissions
public class ComponentEditorActivity extends WithSearchActivity<Searchable>
        implements LoadingListener, ObserableHost {

    private static final String EXTRA_PKG = "ce_extra_pkg";
    private static final String EXTRA_INDEX = "ce_extra_index";

    private final Set<Runnable> dataChangeActions = new HashSet<>(TAB_COUNT);

    public static void start(Context context, String pkg) {
        Intent intent = new Intent(context, ComponentEditorActivity.class);
        intent.putExtra(EXTRA_PKG, pkg);
        context.startActivity(intent);
    }

    private String mPackageName;

    private ComponentListFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.component_editor);

        setupToolbar();
        showHomeAsUp();

        if (!internalResolveIntent()) {
            finish();
            return;
        }
        initView();

        // Workaround to fix o style.
        if (!mUserTheme.isReverseTheme()) {
            initColor();
        }

        initPages();
    }

    private void initView() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentFragment = mFragments.get(position);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentFragment = mFragments.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Post this fucking work, do not block ui work.
        runOnUiThread(() -> {
            setTitle(PkgUtil.loadNameByPkgName(getApplicationContext(), mPackageName));
            setSubTitleChecked(mPackageName);
        });

    }

    private static final int INDEX_SERVICE = 0;
    private static final int INDEX_BROADCAST = 1;
    private static final int INDEX_ACTIVITY = 2;
    private static final int TAB_COUNT = 3;

    @SuppressWarnings("ConstantConditions")
    void setTabTitle(int index, String title) {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.getTabAt(index).setText(title);
    }

    private void initPages() {
        mFragments.clear();
        mFragments.add(ServiceListFragment.newInstance(mPackageName, INDEX_SERVICE));
        mFragments.add(ReceiverListFragment.newInstance(mPackageName, INDEX_BROADCAST));
        mFragments.add(ActivityListFragment.newInstance(mPackageName, INDEX_ACTIVITY));
    }

    private void initColor() {
        // Apply theme color.
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColor());

        // Apply palette color.
        PaletteColorPicker.pickPrimaryColor(this, color1 -> applyColor(color1), mPackageName, color);
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        this.setThemeColor(color);
        AppBarLayout appBar = findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(color);
        int dark = ColorUtil.colorBurn(color);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }
    }

    @Getter
    @Setter
    private int themeColor;

    private boolean internalResolveIntent() {
        Intent intent = getIntent();
        if (intent == null) return false;
        mPackageName = intent.getStringExtra(EXTRA_PKG);
        return !TextUtils.isEmpty(mPackageName);
    }

    @NonNull
    @Override
    public ArrayList<Searchable> findItem(String query, int page) {
        return mCurrentFragment.findItem(query, page);
    }

    @Override
    public void onItemClicked(Searchable item) {
        super.onItemClicked(item);
        mCurrentFragment.onItemClicked(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.component_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            if (mCurrentFragment != null) {
                mSearchView.display();
                openKeyboard();
            }
            return true;
        }

        if (id == R.id.action_export_service_settings) {
            final String formated = ComponentLoader.Impl.create(this).formatServiceSettings(mPackageName);
            showDialog(R.string.title_export_broadcast_settings,
                    formated,
                    R.string.title_export,
                    android.R.string.cancel,
                    R.string.title_copy_to_clipboard,
                    false,
                    () -> {
                        String path = getServiceConfigPath();
                        boolean res = FileUtil.writeString(formated, path);
                        showSimpleDialog(getString(res ? R.string.title_export_success
                                        : R.string.title_export_fail),
                                path);
                    },
                    null,
                    () -> {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cmb != null) {
                            cmb.setPrimaryClip(ClipData.newPlainText("service_config", formated));
                        }
                    });
            return true;
        }

        if (id == R.id.action_export_broadcast_settings) {
            final String formated = ComponentLoader.Impl.create(this).formatReceiverSettings(mPackageName);
            showDialog(R.string.title_export_broadcast_settings,
                    formated,
                    R.string.title_export,
                    android.R.string.cancel,
                    R.string.title_copy_to_clipboard,
                    false,
                    () -> {
                        String path = getBroadcastConfigPath();
                        boolean res = FileUtil.writeString(formated, path);
                        showSimpleDialog(getString(res ? R.string.title_export_success
                                        : R.string.title_export_fail),
                                path);
                    },
                    null,
                    () -> {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cmb != null) {
                            cmb.setPrimaryClip(ClipData.newPlainText("broadcast_config", formated));
                        }
                    });
            return true;
        }

        if (id == R.id.action_export_activity_settings) {
            final String formated = ComponentLoader.Impl.create(this).formatActivitySettings(mPackageName);
            showDialog(R.string.title_export_activity_settings,
                    formated,
                    R.string.title_export,
                    android.R.string.cancel,
                    R.string.title_copy_to_clipboard,
                    false,
                    () -> {
                        String path = getActivityConfigPath();
                        boolean res = FileUtil.writeString(formated, path);
                        showSimpleDialog(getString(res ? R.string.title_export_success
                                        : R.string.title_export_fail),
                                path);
                    },
                    null,
                    () -> {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cmb != null) {
                            cmb.setPrimaryClip(ClipData.newPlainText("activity_config", formated));
                        }
                    });
            return true;
        }

        if (id == R.id.action_import_service_settings) {
            ComponentEditorActivityPermissionRequester.onRequestImportServiceConfigChecked(this);
            return true;
        }

        if (id == R.id.action_import_broadcast_settings) {
            ComponentEditorActivityPermissionRequester.onRequestImportBroadcastConfigChecked(this);
            return true;
        }

        if (id == R.id.action_import_activity_settings) {
            ComponentEditorActivityPermissionRequester.onRequestImportActivityConfigChecked(this);
            return true;
        }

        if (id == R.id.action_disable_all) {
            if (mCurrentFragment != null) {
                mCurrentFragment.onRequestDisableAll();
            }
            return true;
        }

        if (id == R.id.action_enable_all) {
            if (mCurrentFragment != null) {
                mCurrentFragment.onRequestEnabledAll();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ComponentEditorActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    @RequiresPermission.OnDenied("onPermissionDenied")
    void onRequestImportServiceConfig() {
        pickSingleFile(this, REQUEST_CODE_PICK_SERVICE_CONFIG);
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    @RequiresPermission.OnDenied("onPermissionDenied")
    void onRequestImportBroadcastConfig() {
        pickSingleFile(this, REQUEST_CODE_PICK_BROADCAST_CONFIG);
    }

    @RequiresPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE})
    @RequiresPermission.OnDenied("onPermissionDenied")
    void onRequestImportActivityConfig() {
        pickSingleFile(this, REQUEST_CODE_PICK_ACTIVITY_CONFIG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_SERVICE_CONFIG && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            setLastUserSelectPath(file.getPath());
            onServiceConfigPick(file);
        }
        if (requestCode == REQUEST_CODE_PICK_BROADCAST_CONFIG && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            setLastUserSelectPath(file.getPath());
            onBroadcastConfigPick(file);
        }
        if (requestCode == REQUEST_CODE_PICK_ACTIVITY_CONFIG && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            setLastUserSelectPath(file.getPath());
            onActivityConfigPick(file);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onServiceConfigPick(File f) {
        try {
            ServiceInfoSettingsList serviceInfoSettingsList = ServiceInfoSettingsList.fromJson(FileUtil.readString(f.getPath()));
            boolean ok = ComponentUtil.applyBatch(getContext(), serviceInfoSettingsList);
            showTips(ok ? R.string.title_import_success : R.string.title_import_fail, false, null, null);
        } catch (Throwable e) {
            showTips(R.string.title_import_fail, false, null, null);
        } finally {
            notifyChanged();
        }
    }

    private void onBroadcastConfigPick(File f) {
        try {
            ActivityInfoSettingsList activityInfoSettingsList = ActivityInfoSettingsList.fromJson(FileUtil.readString(f.getPath()));
            boolean ok = ComponentUtil.applyBatch(getContext(), activityInfoSettingsList);
            showTips(ok ? R.string.title_import_success : R.string.title_import_fail, false, null, null);
        } catch (Throwable e) {
            showTips(R.string.title_import_fail, false, null, null);
        } finally {
            notifyChanged();
        }
    }

    private void onActivityConfigPick(File f) {
        try {
            ActivityInfoSettingsList activityInfoSettingsList = ActivityInfoSettingsList.fromJson(FileUtil.readString(f.getPath()));
            boolean ok = ComponentUtil.applyBatch(getContext(), activityInfoSettingsList);
            showTips(ok ? R.string.title_import_success : R.string.title_import_fail, false, null, null);
        } catch (Throwable e) {
            showTips(R.string.title_import_fail, false, null, null);
        } finally {
            notifyChanged();
        }
    }

    private static final int REQUEST_CODE_PICK_SERVICE_CONFIG = 0x111;
    private static final int REQUEST_CODE_PICK_BROADCAST_CONFIG = 0x112;
    private static final int REQUEST_CODE_PICK_ACTIVITY_CONFIG = 0x113;

    // FIXME Copy to File utils.
    private static void pickSingleFile(Activity activity, int code) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to getSingleton paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH,
                getLastUserSelectPath() == null
                        ? Environment.getExternalStorageDirectory().getPath()
                        : getLastUserSelectPath());

        activity.startActivityForResult(i, code);
    }

    private static String sLastUserSelectPath = null;

    public static String getLastUserSelectPath() {
        return (sLastUserSelectPath != null && new File(sLastUserSelectPath).exists()) ? sLastUserSelectPath : null;
    }

    public static void setLastUserSelectPath(String path) {
        ComponentEditorActivity.sLastUserSelectPath = path;
    }

    void onPermissionDenied() {
        showTips("onPermissionDenied", false, null, null);
    }

    private String getServiceConfigPath() {
        if (getExternalCacheDir() == null) return null;
        return getExternalCacheDir().getPath() + File.separator
                + mPackageName + ".service_config"; // com.android.mms.service_config
    }

    private String getBroadcastConfigPath() {
        if (getExternalCacheDir() == null) return null;
        return getExternalCacheDir().getPath() + File.separator
                + mPackageName + ".broadcast_config"; // com.android.mms.broadcast_config
    }

    private String getActivityConfigPath() {
        if (getExternalCacheDir() == null) return null;
        return getExternalCacheDir().getPath() + File.separator
                + mPackageName + ".activity_config"; // com.android.mms.activity_config
    }

    private static final int FRAGMENT_COUNT = TAB_COUNT;
    private final List<ComponentListFragment> mFragments = new ArrayList<>(FRAGMENT_COUNT);

    @Override
    public void onLoadingComplete(int index, List data, int enableCount) {
        switch (index) {
            case INDEX_ACTIVITY:
                setTabTitle(index, getString(R.string.tab_text_3) + "[" + enableCount + "/" + data.size() + "]");
                break;
            case INDEX_BROADCAST:
                setTabTitle(index, getString(R.string.tab_text_2) + "[" + enableCount + "/" + data.size() + "]");
                break;
            case INDEX_SERVICE:
                setTabTitle(index, getString(R.string.tab_text_1) + "[" + enableCount + "/" + data.size() + "]");
                break;
        }
    }

    @Override
    public void registerOnDataChangeListener(Runnable action) {
        if (!dataChangeActions.contains(action)) dataChangeActions.add(action);
    }

    @Override
    public void unRegisterOnDataChangeListener(Runnable action) {
        dataChangeActions.remove(action);
    }

    @Override
    public void notifyChanged() {
        Collections.consumeRemaining(dataChangeActions,
                new Consumer<Runnable>() {
                    @Override
                    public void accept(Runnable runnable) {
                        runnable.run();
                    }
                });
    }

    @SuppressWarnings("ConstantConditions")
    public static class ActivityListFragment extends ComponentListFragment {

        public static ActivityListFragment newInstance(String pkg, int index) {
            ActivityListFragment fragment = new ActivityListFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(EXTRA_PKG, pkg);
            bundle.putInt(EXTRA_INDEX, index);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadActivitySettings(getTargetPackageName());
        }

        @Override
        protected ComponentListAdapter onCreateAdapter() {
            return new ActivitySettingsAdapter(getActivity());
        }

        @Override
        public int indexOf(final Searchable serviceInfoSettings) {
            return getComponentListAdapter().getData().indexOf(serviceInfoSettings);
        }
    }

    public static class ReceiverListFragment extends ComponentListFragment {

        public static ReceiverListFragment newInstance(String pkg, int index) {
            ReceiverListFragment fragment = new ReceiverListFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(EXTRA_PKG, pkg);
            bundle.putInt(EXTRA_INDEX, index);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        protected List performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadReceiverSettings(getTargetPackageName());
        }

        @Override
        protected ComponentListAdapter onCreateAdapter() {
            return new ReceiverSettingsAdapter(getActivity());
        }

        @Override
        public int indexOf(final Searchable serviceInfoSettings) {
            return getComponentListAdapter().getData().indexOf(serviceInfoSettings);
        }
    }

    public static class ServiceListFragment extends ComponentListFragment {

        public static ServiceListFragment newInstance(String pkg, int index) {
            ServiceListFragment fragment = new ServiceListFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(EXTRA_PKG, pkg);
            bundle.putInt(EXTRA_INDEX, index);
            fragment.setArguments(bundle);
            return fragment;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected List<ServiceInfoSettings> performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadServiceSettings(getTargetPackageName());
        }

        @Override
        protected ServiceSettingsAdapter onCreateAdapter() {
            return new ServiceSettingsAdapter(getActivity());
        }

        @Override
        public int indexOf(final Searchable serviceInfoSettings) {
            return getComponentListAdapter().getData().indexOf(serviceInfoSettings);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Getter
    public static class ComponentListFragment extends Fragment
            implements SearchViewResults.SearchPerformer<Searchable>,
            onSimpleSearchActionsListener<Searchable> {

        private SwipeRefreshLayout swipeRefreshLayout;
        private ComponentListAdapter componentListAdapter;
        private RecyclerView recyclerView;

        private String targetPackageName;
        private int index;

        @Getter
        private LoadingListener loadingListener;

        public ComponentListFragment() {
        }

        Runnable loadingRunnable = new Runnable() {
            @Override
            public void run() {
                startLoading();
            }
        };

        protected void startLoading() {
            swipeRefreshLayout.setRefreshing(true);
            XExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final List res = performLoading();

                    final int[] enableCount = {0};
                    Collections.consumeRemaining(res, new Consumer() {
                        @Override
                        public void accept(Object o) {
                            if (o instanceof ActivityInfoSettings) {
                                boolean enable = ((ActivityInfoSettings) o).isAllowed();
                                if (enable) enableCount[0]++;
                            } else if (o instanceof ServiceInfoSettings) {
                                boolean enable = ((ServiceInfoSettings) o).isAllowed();
                                if (enable) enableCount[0]++;
                            }
                        }
                    });

                    if (getActivity() == null || isDetached()) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            //noinspection unchecked
                            componentListAdapter.update(res);
                            loadingListener.onLoadingComplete(index, res, enableCount[0]);
                        }
                    });
                }
            });
        }

        protected List performLoading() {
            return Lists.newArrayListWithCapacity(0);
        }

        protected ComponentListAdapter onCreateAdapter() {
            return null;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.targetPackageName = getArguments().getString(EXTRA_PKG);
            this.index = getArguments().getInt(EXTRA_INDEX, -1);
            this.loadingListener = (LoadingListener) getActivity();
            ObserableHost host = (ObserableHost) getActivity();
            host.registerOnDataChangeListener(loadingRunnable);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            ObserableHost host = (ObserableHost) getActivity();
            host.unRegisterOnDataChangeListener(loadingRunnable);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.component_list, container, false);
            setupView(rootView);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Logger.d("onResume: " + getClass().getSimpleName());

        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            startLoading();
        }

        void setupView(View rootView) {
            recyclerView = rootView.findViewById(R.id.recycler_view);
            swipeRefreshLayout = rootView.findViewById(R.id.swipe);
            swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

            componentListAdapter = onCreateAdapter();
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(componentListAdapter);


            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            startLoading();
                        }
                    });
        }

        void onRequestEnabledAll() {
            onRequestEnabledDisableAll(true);
        }

        void onRequestEnabledDisableAll(final boolean enable) {
            if (getActivity() == null) {
                Toast.makeText(XAPMApplication.getApp(), R.string.err_context_null, Toast.LENGTH_LONG).show();
                return;
            }

            final ProgressDialog d = new ProgressDialog(getActivity());
            d.setIndeterminate(true);
            d.setTitle("不要离开");
            d.setMessage("...");
            d.setCancelable(false);
            d.show();

            XExecutor.execute(new Runnable() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    // Use copy data, avoid current modify err.
                    List dataCopy = new ArrayList(getComponentListAdapter().getData());
                    Collections.consumeRemaining(dataCopy,
                            new Consumer() {
                                @Override
                                public void accept(Object o) {

                                    if (isDetached()) return;

                                    ComponentName componentName = null;
                                    if (o instanceof ActivityInfoSettings) {
                                        componentName = ComponentUtil
                                                .getComponentName(((ActivityInfoSettings) o).getActivityInfo());
                                    } else if (o instanceof ServiceInfoSettings) {
                                        componentName = ComponentUtil
                                                .getComponentName(((ServiceInfoSettings) o).getServiceInfo());
                                    }

                                    if (componentName == null) return;

                                    XAPMManager.get().setComponentEnabledSetting(componentName,
                                            enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                            0);

                                    final ComponentName finalComponentName = componentName;

                                    if (getActivity() == null || isDetached()) return;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            d.setMessage(finalComponentName.flattenToShortString());
                                        }
                                    });

                                    try {
                                        Thread.sleep(666);
                                    } catch (InterruptedException ignored) {

                                    }
                                }
                            });

                    // Wait for the action complete.
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {

                    }

                    if (getActivity() == null || isDetached()) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();

                            startLoading();
                        }
                    });
                }
            });
        }

        void onRequestDisableAll() {
            onRequestEnabledDisableAll(false);
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public ArrayList<Searchable> findItem(final String query, int page) {
            ArrayList all = (ArrayList<Searchable>) getComponentListAdapter().getData();
            final ArrayList<Searchable> res = new ArrayList<>();
            Collections.consumeRemaining(all, new Consumer() {
                @Override
                public void accept(Object o) {
                    if (String.valueOf(o).toLowerCase().contains(query.toLowerCase())) {
                        res.add((Searchable) o);
                    }
                }
            });
            return res;
        }

        @Override
        public void onItemClicked(Searchable item) {
            int index = indexOf(item);
            getRecyclerView().scrollToPosition(index);
            getComponentListAdapter().setSelection(index);
        }

        public int indexOf(Searchable item) {
            return 0;
        }

        @Override
        public void onScroll() {

        }

        @Override
        public void error(String localizedMessage) {

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ComponentListFragment (defined as a static inner class below).
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }
    }
}

interface LoadingListener {
    void onLoadingComplete(int tab, List data, int enableCount);
}

interface ObserableHost {
    void registerOnDataChangeListener(Runnable action);

    void unRegisterOnDataChangeListener(Runnable action);

    void notifyChanged();
}