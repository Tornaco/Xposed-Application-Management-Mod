package github.tornaco.xposedmoduletest.ui.activity.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import dev.nick.tiles.tile.Category;
import github.tornaco.permission.requester.RequiresPermission;
import github.tornaco.permission.requester.RuntimePermissions;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.AppCustomDashboardFragment;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyAndroidId;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyApps;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyDeviceId;
import github.tornaco.xposedmoduletest.ui.tiles.PrivacyIccSerial;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */
@RuntimePermissions
@Deprecated
public class PrivacySettingsActivity extends WithWithCustomTabActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, PrivacySettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        PrivacySettingsActivityPermissionRequester.setupViewsChecked(this);
    }

    public static class Dashboards extends AppCustomDashboardFragment {

        @Override
        protected boolean androidPStyleIcon() {
            return false;
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {
            super.onCreateDashCategories(categories);

            Category apps = new Category();
            apps.addTile(new PrivacyApps(getActivity()));

            Category items = new Category();
            items.titleRes = R.string.title_privacy_items;
            items.addTile(new PrivacyAndroidId(getContext()));
            items.addTile(new PrivacyDeviceId(getContext()));
            items.addTile(new PrivacyIccSerial(getContext()));

            categories.add(apps);
            categories.add(items);
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    void setupViews() {
        replaceV4(R.id.container, new Dashboards(), null, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PrivacySettingsActivityPermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void setupViews1() {

        View card = findViewById(R.id.card);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(XAPMManager.get().getAndroidId());

        card.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(new EditTextAction() {
                    @Override
                    public void onAction(String text) {
                        XAPMManager.get().setUserDefinedAndroidId(text);
                        androidIdTextView.setText(R.string.title_privacy_update_later);

                        getUIThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                androidIdTextView.setText(XAPMManager.get().getAndroidId());
                            }
                        }, 1000);
                    }
                });
            }
        });
        card.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAPMManager.get().setUserDefinedAndroidId(null);
                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(XAPMManager.get().getAndroidId());
                    }
                }, 1000);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"MissingPermission", "HardwareIds"})
    void setupViews2() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final View card = findViewById(R.id.card2);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(tm.getDeviceId());

        card.findViewById(R.id.button21).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(new EditTextAction() {
                    @Override
                    public void onAction(String text) {
                        XAPMManager.get().setUserDefinedDeviceId(text);

                        androidIdTextView.setText(R.string.title_privacy_update_later);

                        getUIThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                androidIdTextView.setText(tm.getDeviceId());
                            }
                        }, 1000);
                    }
                });
            }
        });
        card.findViewById(R.id.button22).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAPMManager.get().setUserDefinedDeviceId(null);

                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(tm.getDeviceId());
                    }
                }, 1000);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"MissingPermission", "HardwareIds"})
    void setupViews3() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final View card = findViewById(R.id.card3);

        final TextView androidIdTextView = card.findViewById(android.R.id.text1);
        androidIdTextView.setText(tm.getLine1Number());

        card.findViewById(R.id.button31).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(new EditTextAction() {
                    @Override
                    public void onAction(String text) {
                        XAPMManager.get().setUserDefinedLine1Number(text);

                        androidIdTextView.setText(R.string.title_privacy_update_later);

                        getUIThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                androidIdTextView.setText(tm.getLine1Number());
                            }
                        }, 1000);
                    }
                });
            }
        });
        card.findViewById(R.id.button32).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAPMManager.get().setUserDefinedLine1Number(null);

                androidIdTextView.setText(R.string.title_privacy_update_later);

                getUIThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidIdTextView.setText(tm.getLine1Number());
                    }
                }, 1000);
            }
        });
    }

    private void showEditTextDialog(final EditTextAction action) {
        final EditText e = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_privacy_input_code)
                .setView(e)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = e.getText().toString();
                        action.onAction(text);
                    }
                })
                .show();
    }

    interface EditTextAction {
        void onAction(String text);
    }
}
