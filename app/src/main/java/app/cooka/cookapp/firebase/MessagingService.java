package app.cooka.cookapp.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import app.cooka.cookapp.MainActivity;
import app.cooka.cookapp.R;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    // message data keys
    private static final String MDK_NOTIFICATIONTYPE = "notificationType";

    private static int nextNotificationId = 1;

    public static int getNextNotificationId() {
        return nextNotificationId++;
    }

    /**
     * Called when message is received.
     * @param remoteMessage the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Firebase message from: " + remoteMessage.getFrom());

        // check if message contains a notification payload
        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Firebase message notification body: " + remoteMessage.getNotification().getBody());
        }

        // check if the message contains data
        Map<String, String> messageData = remoteMessage.getData();
        if(messageData != null && messageData.size() > 0) {

            // check if the message contains a notification type key
            if(messageData.containsKey(MDK_NOTIFICATIONTYPE)) {

                // switch through and handle different supported notification types
                String notificationType = messageData.get(MDK_NOTIFICATIONTYPE);
                switch(notificationType) {
                case "welcome":
                    createWelcomeNotification();
                    break;
                }
            }
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        Log.d(TAG, "Firebase Cloud Messaging token: " + token);
        sendRegistrationToServer(token);
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
            .setService(MessagingJobService.class)
            .setTag("my-job-tag")
            .build();
        dispatcher.schedule(myJob);
    }

    /**
     * Callback method used by the Firebase Cloud Messaging service to notify about a new device/
     * user token and to be used for storing that token in the database and associating it with a
     * user of this app.
     * @param token the Firebase Cloud Messaging token generated for this device/user.
     */
    private void sendRegistrationToServer(String token) {

        // todo: associate this token with the logged in user in the database by running dedicated database request
    }

    /**
     * Create and show a simple notification containing the received message from Firebase Cloud
     * Messaging.
     * @param message the message body received from Firebase Cloud Messaging service
     */
    private void createNotification(String channelId, String channelName, String title,
        String message)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        Bitmap cookaLogo = BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_cooka_icon);

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId)
                //.setLargeIcon(cookaLogo)
                .setSmallIcon(R.drawable.ic_cooka_icon)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // since android oreo the notification channel is required
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = notificationBuilder.build();
        notificationManager.notify(nextNotificationId++, notification);
    }

    private void createCustomNotification(String channelId, String channelName, String title,
        String message)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        Bitmap cookaLogo = BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_cooka_icon);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText
            .setBigContentTitle(title)
            .setSummaryText(title)
            .bigText(message);

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId)
                .setContentIntent(pendingIntent)
                .setLargeIcon(cookaLogo)
                .setSmallIcon(R.drawable.ic_cooka_icon)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(bigText)
                .setLights(Color.GREEN, 500, 500)
                .setSound(defaultSoundUri)
                .setChannelId(channelId);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // since android oreo the notification channel is required
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = notificationBuilder.build();
        notificationManager.notify(nextNotificationId++, notification);
    }

    private void createWelcomeNotification() {

        String channelId = "usage";
        String title = getString(R.string.notif_welcome_title);
        String message = getString(R.string.notif_welcome_messagebody);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        PendingIntent explorePendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);
        PendingIntent hashtagsIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);
        PendingIntent peopleIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText
            .setBigContentTitle(title)
            .setSummaryText(title)
            .bigText(message);

        Bitmap cookaLogo = BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_cooka_icon);

        final NotificationCompat.Action exploreAction =
            new NotificationCompat.Action(R.drawable.ic_explore_24px, getString(R.string.notif_action_explore),
                explorePendingIntent);
        final NotificationCompat.Action hashtagsAction =
            new NotificationCompat.Action(R.drawable.ic_hashtag_24px, getString(R.string.notif_action_hashtags),
                explorePendingIntent);
        final NotificationCompat.Action peoplesAction =
            new NotificationCompat.Action(R.drawable.ic_people_24dp, getString(R.string.notif_action_people),
                explorePendingIntent);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationBuilder
            .setContentIntent(pendingIntent)
            .setLargeIcon(cookaLogo)
            .setSmallIcon(R.drawable.ic_cooka_icon)
            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(Notification.PRIORITY_MAX)
            .setStyle(bigText)
            .setLights(Color.GREEN, 500, 500)
            .setSound(defaultSoundUri)
            .addAction(exploreAction)
            .addAction(hashtagsAction)
            .addAction(peoplesAction)
            .setAutoCancel(true)
            .setChannelId(channelId);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(nextNotificationId++, notification);
    }
}
