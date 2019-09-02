package net.crow31415.emergencyNotification_app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.activity.MainActivity;

import java.util.Map;

import androidx.core.app.NotificationCompat;

public class FcmListenerService extends FirebaseMessagingService {

    private final static String TAG = FcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map data = message.getData();

        Log.d(TAG, "from:" + from);
        Log.d(TAG, "data:" + data.toString());

        sendNotification();
    }

    private void sendNotification() {
        // 通知設定
        Intent intent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        String channelId = "push";
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_detect_fell_user_registered))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= 26 ) {
            CharSequence name = getString(R.string.channel_name_push);
            String description = getString(R.string.channel_description_push);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(2, notification);
    }
}
