package github.tornaco.xposedmoduletest.ui.tiles.app;

import android.content.Context;
import android.view.View;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD3ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.licenses.MozillaPublicLicense20;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.opensource.GlideLicense;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class OpenSourceLicenses extends QuickTile {

    public OpenSourceLicenses(final Context context) {
        super(context);
        this.titleRes = R.string.title_open_source_lisenses;
        this.iconRes = R.drawable.ic_short_text_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            protected int getImageViewBackgroundRes() {
                return R.drawable.tile_bg_blue_grey;
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                final Notices notices = new Notices();

                notices.addNotice(
                        new Notice(
                                "X-APM",
                                "https://github.com/Tornaco/X-APM",
                                null,
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "Lombok",
                                "https://projectlombok.org/",
                                " Copyright © 2009-2018 The Project Lombok Authors",
                                new MITLicense()));

                notices.addNotice(
                        new Notice(
                                "guava",
                                "https://github.com/google/guava",
                                null,
                                new MITLicense()));

                notices.addNotice(
                        new Notice(
                                "retrofit",
                                "https://github.com/square/retrofit",
                                "Copyright 2013 Square, Inc.",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "RxJava",
                                "https://github.com/ReactiveX/RxJava",
                                "Copyright (c) 2016-present, RxJava Contributors.",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "RxAndroid",
                                "https://github.com/ReactiveX/RxAndroid",
                                "Copyright 2015 The RxAndroid authors",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "jsoup",
                                "https://github.com/jhy/jsoup",
                                null,
                                new MITLicense()));

                notices.addNotice(
                        new Notice(
                                "RecyclerView-FastScroll",
                                "https://github.com/timusus/RecyclerView-FastScroll",
                                null,
                                new MITLicense()));

                notices.addNotice(
                        new Notice(
                                "NoNonsense-FilePicker",
                                "https://github.com/spacecowboy/NoNonsense-FilePicker",
                                null,
                                new MozillaPublicLicense20()));

                notices.addNotice(
                        new Notice(
                                "AndroidShell",
                                "https://github.com/jaredrummler/AndroidShell",
                                "Copyright (C) 2016 Jared Rummler\n" +
                                        "Copyright (C) 2012-2015 Jorrit \"Chainfire\" Jongma",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "glide",
                                "https://github.com/bumptech/glide",
                                null,
                                new GlideLicense()));

                notices.addNotice(
                        new Notice(
                                "Emoji",
                                "https://github.com/vanniktech/Emoji",
                                "Copyright (C) 2016 Vanniktech - Niklas Baudy",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "bottomsheet",
                                "https://github.com/Flipboard/bottomsheet",
                                null,
                                new BSD3ClauseLicense()));

                notices.addNotice(
                        new Notice(
                                "expandable-recycler-view",
                                "https://github.com/thoughtbot/expandable-recycler-view",
                                "Copyright (c) 2016 thoughtbot, inc.",
                                new MITLicense()));

                notices.addNotice(
                        new Notice(
                                "Badge",
                                "https://github.com/nekocode/Badge",
                                null,
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "material-searchview",
                                "https://github.com/Shahroz16/material-searchview",
                                " Copyright (C) 2016 Tim Malseed",
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "PatternLockView",
                                "https://github.com/aritraroy/PatternLockView",
                                null,
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "PinLockView",
                                "https://github.com/aritraroy/PinLockView",
                                null,
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "greenDAO",
                                "https://github.com/greenrobot/greenDAO",
                                null,
                                new ApacheSoftwareLicense20()));

                notices.addNotice(
                        new Notice(
                                "ImagePicker",
                                "https://github.com/Mariovc/ImagePicker",
                                "Copyright 2016 Mario Velasco Casquero",
                                new ApacheSoftwareLicense20()));


//                notices.addNotice(
//                        new Notice(
//                                "LicensesDialog",
//                                "http://psdev.de",
//                                "Copyright 2013 Philip Schiffer <admin@psdev.de>",
//                                new ApacheSoftwareLicense20()));

//                notices.addNotice(
//                        new Notice(
//                                "Test 2",
//                                "http://example.org",
//                                "Example Person 2",
//                                new GnuLesserGeneralPublicLicense21()));

                new LicensesDialog.Builder(context)
                        .setNotices(notices)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
            }
        };
    }
}
