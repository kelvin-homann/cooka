package app.cooka.cookapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class NotificationUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel createAppNotificationChanel(final String chanelId, final String
        chanelName, final String chanelDescription, final int chanelImportance)
    {
        NotificationChannel channel = new NotificationChannel(chanelId, chanelName,
            chanelImportance);
        channel.setDescription(chanelDescription);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void registerNotificationChannel(final Context context, final NotificationChannel
        notificationChannel)
    {
        if(notificationChannel == null)
            return;
        final NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void registerNotificationChannels(final Context context, final List<NotificationChannel>
        notificationChannels)
    {
        if(notificationChannels == null || notificationChannels.size() == 0)
            return;
        final NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.createNotificationChannels(notificationChannels);
    }
}
