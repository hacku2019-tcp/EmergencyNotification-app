package net.crow31415.emergencyNotification_app.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import net.crow31415.emergencyNotification_app.R;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AccelerationMeasureService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private String logTAG;

    @Override
    public void onCreate() {
        super.onCreate();
        logTAG = getResources().getString(R.string.app_name);
        Log.d(logTAG, "called AccelerationMeasureService.onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(logTAG, "called AccelerationMeasureService.onStartCommand()");
        String channelId = "service";

        // 通知設定
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.acceleration_measure_service_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        // フォアグラウンドで実行
        startForeground(1, notification);

        // センサーの取得
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensorAcceleration != null){
            // センサーへのイベントリスナーを設定
            sensorManager.registerListener(this, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensorGravity != null){
            // センサーへのイベントリスナーを設定
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
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
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                Log.d(logTAG, "called AccelerationMeasureService.onSensorChanged (x:" + x + " y:" + y + " z:" + z + ")");

                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                Log.d(logTAG, "acceleration: " + acceleration);
                break;

            case Sensor.TYPE_GRAVITY:
                float g = event.values[0];
                Log.d(logTAG, "called AccelerationMeasureService.onSensorChanged (g:" + g + ")");
                Log.d(logTAG, "gravity: " + g);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
