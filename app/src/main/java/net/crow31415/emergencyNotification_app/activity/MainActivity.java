package net.crow31415.emergencyNotification_app.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;

public class MainActivity extends AppCompatActivity {

    private final MainActivity self = this;
    private FirebaseAnalytics mAnalytics;
    private EditText tokenEditText;
    private Button registerButton;
    private Button serviceButton;

    private boolean serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAnalytics = FirebaseAnalytics.getInstance(self);
        tokenEditText = findViewById(R.id.token_edit_text);
        registerButton = findViewById(R.id.button_register);
        serviceButton = findViewById(R.id.button_toggle_service);

        checkServiceRunning();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(tokenEditText.getText().toString());
                checkServiceRunning();
                if(!serviceRunning){
                    startMeasureService();
                    toggleServiceButton();
                }
            }
        });

        serviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(serviceRunning){
                    //Stop Button
                    stopMeasureService();
                }else {
                    //Start Button
                    startMeasureService();
                }
                toggleServiceButton();
            }
        });

    }

    private void startMeasureService(){
        Intent serviceIntent = new Intent(self, AccelerationMeasureService.class);
        if(Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent);
        }else{
            startService(serviceIntent);
        }
    }

    private void stopMeasureService(){
        Intent serviceIntent = new Intent(self, AccelerationMeasureService.class);
        stopService(serviceIntent);
    }

    public void register(String token){

    }

    public void checkServiceRunning(){
        // Service実行状態確認
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AccelerationMeasureService.class.getName().equals(serviceInfo.service.getClassName())) {
                // Service実行中
                serviceRunning = true;
                break;
            }
        }
        changeServiceButton(); // Service実行状態反映
    }

    private void toggleServiceButton(){
        serviceRunning = !serviceRunning;
        changeServiceButton();
    }

    private void changeServiceButton(){
        if(serviceRunning){
            //Stop Button
            serviceButton.setText(R.string.btn_stop_service);
        }else {
            //Start Button
            serviceButton.setText(R.string.btn_start_service);
        }
    }
}
