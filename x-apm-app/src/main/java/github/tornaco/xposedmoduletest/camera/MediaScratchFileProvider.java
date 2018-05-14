/*
 * Copyright (C) 2015 The Android Open Source Project
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

package github.tornaco.xposedmoduletest.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.XAPMApplication;

/**
 * A very simple content provider that can serve media files from our cache directory.
 */
public class MediaScratchFileProvider extends FileProvider {
    public static final String AUTHORITY =
            "com.android.messaging.datamodel.MediaScratchFileProvider";
    private static final String TAG = "ScratchFilePro";
    private static final SimpleArrayMap<Uri, String> sUriToDisplayNameMap =
            new SimpleArrayMap<Uri, String>();
    private static final String MEDIA_SCRATCH_SPACE_DIR = "mediascratchspace";

    public static boolean isMediaScratchSpaceUri(final Uri uri) {
        if (uri == null) {
            return false;
        }

        final List<String> segments = uri.getPathSegments();
        return (TextUtils.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT) &&
                TextUtils.equals(uri.getAuthority(), AUTHORITY) &&
                segments.size() == 1 && isValidFileId(segments.get(0)));
    }

    /**
     * Returns a uri that can be used to access a raw mms file.
     *
     * @return the URI for an raw mms file
     */
    public static Uri buildMediaScratchSpaceUri(final String extension) {
        final Uri uri = buildFileUri(AUTHORITY, extension);
        final File file = getFileWithExtension(uri.getPath(), extension);
        if (!ensureFileExists(file)) {
            Log.e(TAG, "Failed to create temp file " + file.getAbsolutePath());
        }
        return uri;
    }

    public static File getFileFromUri(final Uri uri) {
        return getFileWithExtension(uri.getPath(), getExtensionFromUri(uri));
    }

    public static Uri.Builder getUriBuilder() {
        return (new Uri.Builder()).authority(AUTHORITY).scheme(ContentResolver.SCHEME_CONTENT);
    }

    private static File getFileWithExtension(final String path, final String extension) {
        final Context context = XAPMApplication.getApp().getApplicationContext();
        return new File(getDirectory(context),
                TextUtils.isEmpty(extension) ? path : path + "." + extension);
    }

    private static File getDirectory(final Context context) {
        return new File(context.getCacheDir(), MEDIA_SCRATCH_SPACE_DIR);
    }

    public static void addUriToDisplayNameEntry(final Uri scratchFileUri,
                                                final String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return;
        }
        synchronized (sUriToDisplayNameMap) {
            sUriToDisplayNameMap.put(scratchFileUri, displayName);
        }
    }

    @Override
    File getFile(final String path, final String extension) {
        return getFileWithExtension(path, extension);
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
                        final String[] selectionArgs, final String sortOrder) {
        if (projection != null && projection.length > 0 &&
                TextUtils.equals(projection[0], OpenableColumns.DISPLAY_NAME) &&
                isMediaScratchSpaceUri(uri)) {
            // Retrieve the display name associated with a temp file. This is used by the Contacts
            // ImportVCardActivity to retrieve the name of the contact(s) being imported.
            String displayName;
            synchronized (sUriToDisplayNameMap) {
                displayName = sUriToDisplayNameMap.get(uri);
            }
            if (!TextUtils.isEmpty(displayName)) {
                MatrixCursor cursor =
                        new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME});
                RowBuilder row = cursor.newRow();
                row.add(displayName);
                return cursor;
            }
        }
        return null;
    }
}
