package net.crow31415.emergencyNotification_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;

import java.util.Locale;

public class FellDetectedActivity extends AppCompatActivity {

    TextView countText;
    String logTAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fell_detected);
        logTAG = getResources().getString(R.string.app_name);

        countText = findViewById(R.id.textView_countdown);
        Button notEmergencyButton = findViewById(R.id.button_not_emergency);

        notEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CountDown countDown = new CountDown(30 * 1000, 100);
        countDown.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent serviceIntent = new Intent(getApplication(), AccelerationMeasureService.class);
        if(Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent);
        }else{
            startService(serviceIntent);
        }
    }

    public void callAPI(){

    }

    class CountDown extends CountDownTimer {

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            callAPI();
            finish();
        }

        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {

            Log.d(logTAG, "countdown millisUntilFinished: " + millisUntilFinished);

            long ss = millisUntilFinished / 1000 % 60;
            long ms = millisUntilFinished - ss * 1000;
            ms = ms / 100; //表示桁数減らし
            countText.setText(String.format(Locale.JAPAN, "%2d.%d", ss, ms));
        }
    }
}