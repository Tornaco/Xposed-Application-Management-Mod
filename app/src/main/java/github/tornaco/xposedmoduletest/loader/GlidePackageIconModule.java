package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.signature.ObjectKey;

import org.newstand.logger.Logger;

import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.util.BitmapUtil;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XApp;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/12/23.
 * Email: Tornaco@163.com
 */
@GlideModule
public class GlidePackageIconModule extends AppGlideModule {

    @AllArgsConstructor
    private static class PackageInfoDataFetcher implements DataFetcher<Bitmap> {

        @Getter
        private CommonPackageInfo info;

        @Override
        public void loadData(Priority priority, DataCallback<? super Bitmap> callback) {
            Logger.d("loadData: " + info.getPkgName());
            if (info.getPkgName() == null) {
                callback.onLoadFailed(new NullPointerException("Package name is null"));
                return;
            }
            Drawable d = ApkUtil.loadIconByPkgName(XApp.getApp().getApplicationContext(), info.getPkgName());
            Bitmap bd = BitmapUtil.getBitmap(XApp.getApp().getApplicationContext(), d);
            callback.onDataReady(bd);
        }

        @Override
        public void cleanup() {

        }

        @Override
        public void cancel() {

        }

        @NonNull
        @Override
        public Class<Bitmap> getDataClass() {
            return Bitmap.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    private static class PackageIconModuleLoaderFactory
            implements ModelLoaderFactory<CommonPackageInfo, Bitmap> {

        private static Singleton<ModelLoader<CommonPackageInfo, Bitmap>> sLoader
                = new Singleton<ModelLoader<CommonPackageInfo, Bitmap>>() {
            @Override
            protected ModelLoader<CommonPackageInfo, Bitmap> create() {
                return new ModelLoader<CommonPackageInfo, Bitmap>() {
                    @NonNull
                    @Override
                    public LoadData<Bitmap> buildLoadData(CommonPackageInfo info, int width,
                                                          int height, Options options) {
                        Key diskCacheKey = new ObjectKey(info.getPkgName());

                        return new LoadData<>(diskCacheKey, new PackageInfoDataFetcher(info));
                    }

                    @Override
                    public boolean handles(CommonPackageInfo info) {
                        return info.getPkgName() != null;
                    }
                };
            }
        };


        private static ModelLoader<CommonPackageInfo, Bitmap> singleInstanceLoader() {
            return sLoader.get();
        }

        @Override
        public ModelLoader<CommonPackageInfo, Bitmap> build(MultiModelLoaderFactory multiFactory) {
            return singleInstanceLoader();
        }

        @Override
        public void teardown() {

        }
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.append(CommonPackageInfo.class,
                Bitmap.class, new PackageIconModuleLoaderFactory());
    }
}
