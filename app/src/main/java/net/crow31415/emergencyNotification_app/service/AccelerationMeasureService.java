package net.crow31415.emergencyNotification_app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import net.crow31415.emergencyNotification_app.R;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AccelerationMeasureService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private String logTAG;
    private int emergencyThreshold = 30;

    @Override
    public void onCreate() {
        super.onCreate();
        logTAG = getResources().getString(R.string.app_name);
        Log.d(logTAG, "called AccelerationMeasureService.onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(logTAG, "called AccelerationMeasureService.onStartCommand()");
        String channelId = "detection";

        // 通知設定
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.acceleration_measure_service_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        if (Build.VERSION.SDK_INT >= 26 ) {
            CharSequence name = getString(R.string.channel_name_detection);
            String description = getString(R.string.channel_description_detection);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // フォアグラウンドで実行
        startForeground(1, notification);

        // センサーの取得
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensorAcceleration != null){
            // センサーへのイベントリスナーを設定
            sensorManager.registerListener(this, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(logTAG, "called AccelerationMeasureService.onDestroy()");

        // センサーへのイベントリスナーの解除
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            Log.d(logTAG, "acceleration: " + acceleration);

            if(acceleration >= emergencyThreshold){
                Toast.makeText(getApplicationContext() , "転倒検知 acceleration: " + acceleration, Toast.LENGTH_LONG).show();
                emergencyNotification();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void emergencyNotification(){
        Log.d(logTAG, "called AccelerationMeasureService.emergencyNotification()");
        Log.i(logTAG, "A fall was detected.");

    }
}
