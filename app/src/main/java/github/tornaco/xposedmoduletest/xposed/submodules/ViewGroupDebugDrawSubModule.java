package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */
public class ViewGroupDebugDrawSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookOnDebugDraw();
    }

    private void hookOnDebugDraw() {
        XposedLog.verbose("hookOnDebugDraw...");
        try {
            final Class clz = XposedHelpers.findClass("android.view.ViewGroup", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "onDebugDraw", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Paint paint = (Paint) XposedHelpers.callStaticMethod(clz, "getDebugPaint");
                            Canvas canvas = (Canvas) param.args[0];
                            Log.d(XposedLog.TAG_VIEW, "Debug paint: " + paint + ", canvas: " + canvas);
                            if (paint != null && canvas != null) {
                                ViewGroup thisViewGroup = (ViewGroup) param.thisObject;
                                float size = paint.getTextSize();
                                try {
                                    paint.setTextSize(size * 2f);
                                    drawViewIdWorld(thisViewGroup, canvas, paint);
                                } finally {
                                    paint.setTextSize(size);
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookOnDebugDraw OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookOnDebugDraw:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void drawViewIdWorld(ViewGroup viewGroup, Canvas canvas, Paint paint) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View c = viewGroup.getChildAt(i);
            if (c.getVisibility() != View.GONE) {
                if (c instanceof ViewGroup) {
                    drawViewIdWorld((ViewGroup) c, canvas, paint);
                } else {
                    StringBuilder out = new StringBuilder("ID:");
                    final Resources r = c.getResources();
                    final int id = c.getId();
                    if (id > 0 && Resources.resourceHasPackage(id) && r != null) {
                        try {
                            String pkgname;
                            switch (id & 0xff000000) {
                                case 0x7f000000:
                                    pkgname = "app";
                                    break;
                                case 0x01000000:
                                    pkgname = "android";
                                    break;
                                default:
                                    pkgname = r.getResourcePackageName(id);
                                    break;
                            }
                            String typename = r.getResourceTypeName(id);
                            String entryname = r.getResourceEntryName(id);
                            out.append(" ");
                            out.append(pkgname);
                            out.append(":");
                            out.append(typename);
                            out.append("/");
                            out.append(entryname);
                        } catch (Resources.NotFoundException ignored) {
                        }
                    }
                    canvas.drawText(out.toString(), c.getX(), c.getY(), paint);
                }
            }
        }
    }
}
