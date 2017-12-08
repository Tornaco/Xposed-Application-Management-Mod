package github.tornaco.xposedmoduletest.ui.activity.comp;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import github.tornaco.xposedmoduletest.model.SampleModel;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettingsList;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.ui.adapter.ActivitySettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ComponentListAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ReceiverSettingsAdapter;
import github.tornaco.xposedmoduletest.ui.adapter.ServiceSettingsAdapter;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import lombok.Getter;
import lombok.Setter;

@RuntimePermissions
public class ComponentEditorActivity extends BaseActivity implements LoadingListener, ObserableHost {

    private static final String EXTRA_PKG = "ce_extra_pkg";
    private static final String EXTRA_INDEX = "ce_extra_index";

    private final Set<Runnable> dataChangeActions = new HashSet<>(TAB_COUNT);

    public static void start(Context context, String pkg) {
        Intent intent = new Intent(context, ComponentEditorActivity.class);
        intent.putExtra(EXTRA_PKG, pkg);
        context.startActivity(intent);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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

        initColor();

        initPages();
    }

    private void initView() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentFragment = mFragments.get(position);
                Logger.d("onPageScrolled: " + mCurrentFragment);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentFragment = mFragments.get(position);
                Logger.d("onPageSelected: " + mCurrentFragment);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Post this fucking work, do not block ui work.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(PkgUtil.loadNameByPkgName(getApplicationContext(), mPackageName));
            }
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
        int color = ContextCompat.getColor(this, XSettings.getThemes(this).getThemeColorRes());

        // Apply palette color.
        PaletteColorPicker.pickPrimaryColor(this, new PaletteColorPicker.PickReceiver() {
            @Override
            public void onColorReady(int color) {
                applyColor(color);
            }
        }, mPackageName, color);
    }

    @SuppressWarnings("ConstantConditions")
    private void applyColor(int color) {
        this.setThemeColor(color);
        AppBarLayout appBar = findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(color);
        getWindow().setStatusBarColor(ColorUtil.colorBurn(color));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.component_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            if (mCurrentFragment != null) {
                mCurrentFragment.onRequestSearch();
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
                    new Runnable() {
                        @Override
                        public void run() {
                            String path = getServiceConfigPath();
                            boolean res = FileUtil.writeString(formated, path);
                            showSimpleDialog(getString(res ? R.string.title_export_success
                                            : R.string.title_export_fail),
                                    path);
                        }
                    },
                    null,
                    new Runnable() {
                        @Override
                        public void run() {
                            ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cmb != null) {
                                cmb.setPrimaryClip(ClipData.newPlainText("service_config", formated));
                            }
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
                    new Runnable() {
                        @Override
                        public void run() {
                            String path = getBroadcastConfigPath();
                            boolean res = FileUtil.writeString(formated, path);
                            showSimpleDialog(getString(res ? R.string.title_export_success
                                            : R.string.title_export_fail),
                                    path);
                        }
                    },
                    null,
                    new Runnable(
                    ) {
                        @Override
                        public void run() {
                            ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cmb != null) {
                                cmb.setPrimaryClip(ClipData.newPlainText("broadcast_config", formated));
                            }
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
                    new Runnable() {
                        @Override
                        public void run() {
                            String path = getActivityConfigPath();
                            boolean res = FileUtil.writeString(formated, path);
                            showSimpleDialog(getString(res ? R.string.title_export_success
                                            : R.string.title_export_fail),
                                    path);
                        }
                    },
                    null,
                    new Runnable(
                    ) {
                        @Override
                        public void run() {
                            ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cmb != null) {
                                cmb.setPrimaryClip(ClipData.newPlainText("activity_config", formated));
                            }
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
            onServiceConfigPick(file);
        }
        if (requestCode == REQUEST_CODE_PICK_BROADCAST_CONFIG && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            onBroadcastConfigPick(file);
        }
        if (requestCode == REQUEST_CODE_PICK_ACTIVITY_CONFIG && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
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
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, code);
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
    public void onLoadingComplete(int index, List data) {
        switch (index) {
            case INDEX_ACTIVITY:
                setTabTitle(index, getString(R.string.tab_text_3) + "[" + data.size() + "]");
                break;
            case INDEX_BROADCAST:
                setTabTitle(index, getString(R.string.tab_text_2) + "[" + data.size() + "]");
                break;
            case INDEX_SERVICE:
                setTabTitle(index, getString(R.string.tab_text_1) + "[" + data.size() + "]");
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

        @SuppressWarnings("unchecked")
        @Override
        void onRequestSearch() {
            super.onRequestSearch();
            final ArrayList<ActivityInfoSettings> adapterData = (ArrayList<ActivityInfoSettings>)
                    getComponentListAdapter().getData();

            final SimpleSearchDialogCompat<ActivityInfoSettings> searchDialog =
                    new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                            getString(R.string.title_search_hint), null, adapterData,
                            new SearchResultListener<ActivityInfoSettings>() {

                                @Override
                                public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                       ActivityInfoSettings serviceInfoSettings, int i) {
                                    int index = indexOf(serviceInfoSettings);
                                    getRecyclerView().scrollToPosition(index + 1);
                                    getComponentListAdapter().setSelection(index);
                                    baseSearchDialogCompat.dismiss();
                                }
                            });


            searchDialog.show();
            searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
        }

        private int indexOf(final ActivityInfoSettings serviceInfoSettings) {
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

        @SuppressWarnings("unchecked")
        @Override
        void onRequestSearch() {
            super.onRequestSearch();
            final ArrayList<ActivityInfoSettings> adapterData = (ArrayList<ActivityInfoSettings>)
                    getComponentListAdapter().getData();

            final SimpleSearchDialogCompat<ActivityInfoSettings> searchDialog =
                    new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                            getString(R.string.title_search_hint), null, adapterData,
                            new SearchResultListener<ActivityInfoSettings>() {

                                @Override
                                public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                       ActivityInfoSettings serviceInfoSettings, int i) {
                                    int index = indexOf(serviceInfoSettings);
                                    getRecyclerView().scrollToPosition(index + 1);
                                    getComponentListAdapter().setSelection(index);
                                    baseSearchDialogCompat.dismiss();
                                }
                            });


            searchDialog.show();
            searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
        }

        private int indexOf(final ActivityInfoSettings serviceInfoSettings) {
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

        @Override
        protected List<ServiceInfoSettings> performLoading() {
            return ComponentLoader.Impl.create(getActivity().getApplicationContext())
                    .loadServiceSettings(getTargetPackageName());
        }

        @Override
        protected ServiceSettingsAdapter onCreateAdapter() {
            return new ServiceSettingsAdapter(getActivity());
        }

        void provideSimpleDialog() {
            SimpleSearchDialogCompat dialog = new SimpleSearchDialogCompat(getActivity(), "Search...",
                    "What are you looking for...?", null, createSampleData(),
                    new SearchResultListener<SampleModel>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat dialog,
                                               SampleModel item, int position) {
                            Toast.makeText(getActivity(), item.getTitle(),
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
            dialog.show();
            dialog.getSearchBox().setTypeface(Typeface.SERIF);
        }

        private ArrayList<SampleModel> createSampleData() {
            ArrayList<SampleModel> items = new ArrayList<>();
            items.add(new SampleModel("First item"));
            items.add(new SampleModel("Second item"));
            items.add(new SampleModel("Third item"));
            items.add(new SampleModel("The ultimate item"));
            items.add(new SampleModel("Last item"));
            items.add(new SampleModel("Lorem ipsum"));
            items.add(new SampleModel("Dolor sit"));
            items.add(new SampleModel("Some random word"));
            items.add(new SampleModel("guess who's back"));
            return items;
        }

        @SuppressWarnings("unchecked")
        @Override
        void onRequestSearch() {
            super.onRequestSearch();
            final ArrayList<ServiceInfoSettings> adapterData = (ArrayList<ServiceInfoSettings>)
                    getComponentListAdapter().getData();

            final SimpleSearchDialogCompat<ServiceInfoSettings> searchDialog =
                    new SimpleSearchDialogCompat(getActivity(), getString(R.string.title_search),
                            getString(R.string.title_search_hint), null, adapterData,
                            new SearchResultListener<ServiceInfoSettings>() {

                                @Override
                                public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat,
                                                       ServiceInfoSettings serviceInfoSettings, int i) {
                                    int index = indexOf(serviceInfoSettings);
                                    getRecyclerView().scrollToPosition(index + 1);
                                    getComponentListAdapter().setSelection(index);
                                    baseSearchDialogCompat.dismiss();
                                }
                            });


            searchDialog.show();
            searchDialog.getSearchBox().setTypeface(Typeface.SERIF);
        }

        private int indexOf(final ServiceInfoSettings serviceInfoSettings) {
            return getComponentListAdapter().getData().indexOf(serviceInfoSettings);
        }
    }

    @Getter
    public static class ComponentListFragment extends Fragment {

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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            //noinspection unchecked
                            componentListAdapter.update(res);
                            loadingListener.onLoadingComplete(index, res);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        void onRequestSearch() {

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
    void onLoadingComplete(int tab, List data);
}

interface ObserableHost {
    void registerOnDataChangeListener(Runnable action);

    void unRegisterOnDataChangeListener(Runnable action);

    void notifyChanged();
}