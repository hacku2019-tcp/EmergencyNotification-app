package net.crow31415.emergencyNotification_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;

import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class FellDetectedActivity extends AppCompatActivity {

    private final static String TAG = FellDetectedActivity.class.getSimpleName();

    private TextView countText;
    private CountDown countDown;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fell_detected);
        Log.d(TAG, "called FellDetectedActivity.onCreate()");

        countText = findViewById(R.id.textView_countdown);
        vibrator = getSystemService(Vibrator.class);
        Button notEmergencyButton = findViewById(R.id.button_not_emergency);

        notEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //スリープ復帰処理
        Window window = getWindow();
        if(Build.VERSION.SDK_INT >= 27){
            this.setTurnScreenOn(true);
            this.setShowWhenLocked(true);
        }else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //通知音鳴動
        Ringtone ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        ringtone.play();

        //バイブ鳴動
        long[] timings = {1000, 1500};
        if(Build.VERSION.SDK_INT >= 26){
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(timings, 0);
            vibrator.vibrate(vibrationEffect);
        }else {
            vibrator.vibrate(timings, 0);
        }

        countDown = new CountDown(30 * 1000, 100);
        countDown.start();
        Log.d(TAG, "Countdown start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "called FellDetectedActivity.onDestroy()");

        countDown.cancel();

        Intent serviceIntent = new Intent(getApplication(), AccelerationMeasureService.class);
        if(Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent);
        }else{
            startService(serviceIntent);
        }
    }

    public void sendNotification(){
        Log.d(TAG, "called FellDetectedActivity.sendNotification()");

        Intent intent = new Intent(this, SendNotificationActivity.class)
                .setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    class CountDown extends CountDownTimer {

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            sendNotification();
            finish();
        }

        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            long ss = millisUntilFinished / 1000 % 60;
            long ms = millisUntilFinished - ss * 1000;
            ms = ms / 100; //表示桁数減らし
            countText.setText(String.format(Locale.JAPAN, "%2d.%d", ss, ms));
        }
    }
}
