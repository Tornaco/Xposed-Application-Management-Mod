/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.tornaco.vangogh.loader.network;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;

import dev.tornaco.vangogh.common.Error;
import dev.tornaco.vangogh.common.ErrorListener;
import dev.tornaco.vangogh.common.ProgressListener;

public class HttpImageDownloader implements ImageDownloader<String> {

    private File mTmpDir;
    private ByteReadingListener mByteReadingListener;

    public HttpImageDownloader(File tmpDir, ByteReadingListener listener) {
        this.mTmpDir = tmpDir;
        this.mByteReadingListener = listener;
    }

    @Override
    public String download(String url, ProgressListener progressListener,
                           ErrorListener errorListener) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.connect();
            InputStream is = conn.getInputStream();
            float fileSize = (float) conn.getContentLength();
            if (fileSize < 1 || is == null) {
                if (errorListener != null) {
                    errorListener.onError(Error.builder()
                            .errorCode(Error.ERR_CODE_IO)
                            .throwable(new Throwable(String.format("Content for from %s length is 0.", url)))
                            .build());
                }
            } else {
                File tmpFile = new File(mTmpDir, String.valueOf(url.hashCode()));
                Logger.v("Using tmp path:" + tmpFile.getPath());
                FileOutputStream fos = new FileOutputStream(tmpFile);
                byte[] bytes = new byte[1024];
                int len = -1;
                float downloadSize = 0f;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    if (mByteReadingListener != null) {
                        mByteReadingListener.onBytesRead(bytes);
                    }
                    downloadSize += len;
                    float progress = downloadSize / fileSize;
                    if (progressListener != null) {
                        progressListener.onProgressUpdate(progress);
                    }
                }
                is.close();
                fos.close();
                return tmpFile.getPath();
            }
        } catch (InterruptedIOException ignored) {
        } catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(Error.builder()
                        .throwable(e)
                        .errorCode(Error.ERR_CODE_GENERIC)
                        .build());
            }
        }
        return null;
    }

    @Override
    public long size(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.connect();
            return conn.getContentLength();
        } catch (Exception ignored) {

        }
        return -1;
    }

    public interface ByteReadingListener {
        void onBytesRead(byte[] bytes);
    }
}
