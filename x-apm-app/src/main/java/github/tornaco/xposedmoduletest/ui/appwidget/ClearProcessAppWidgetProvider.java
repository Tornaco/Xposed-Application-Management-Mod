package github.tornaco.xposedmoduletest.ui.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.service.WidgetService;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class ClearProcessAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Logger.d("ClearProcessAppWidgetProvider, onUpdate");

        RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_clear_process);
        remoteView.setOnClickPendingIntent(R.id.imageView, getPendingIntent(context));
        appWidgetManager.updateAppWidget(appWidgetIds, remoteView);
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(WidgetService.ACTION_CLEAR_PROCESS);
        return PendingIntent.getService(context, UniqueIdFactory.getNextId(), intent, 0);
    }
}
