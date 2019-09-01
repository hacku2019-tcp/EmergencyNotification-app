package net.crow31415.emergencyNotification_app.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;
import net.crow31415.emergencyNotification_app.util.HTTPSUtility;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final MainActivity self = this;
    private FirebaseAnalytics mAnalytics;
    private String TAG;
    private TextView idTextView;
    private EditText idEditText;
    private Button applyIDButton;
    private EditText noticeUserEditText;
    private Button registerButton;
    private Button serviceButton;
    private SharedPreferences preferences;

    private boolean serviceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAnalytics = FirebaseAnalytics.getInstance(self);
        TAG = getResources().getString(R.string.app_name);
        idTextView = findViewById(R.id.id_text_view);
        idEditText = findViewById(R.id.id_edit_text);
        applyIDButton = findViewById(R.id.button_apply_id);
        noticeUserEditText = findViewById(R.id.notice_user_edit_text);
        registerButton = findViewById(R.id.button_register);
        serviceButton = findViewById(R.id.button_toggle_service);
        preferences = getSharedPreferences("net.crow31415.emergencyNotification_app.preferences", MODE_PRIVATE);

        initID();
        checkServiceRunning();

        applyIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyID(idEditText.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(noticeUserEditText.getText().toString());
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

    private void initID(){
        if(preferences.getString("id", null) == null){
            int r = (int)(Math.random() * 10000);
            applyID(String.format(Locale.JAPAN, "NoID%5d", r));
        }
        applyID("");
    }

    private void applyID(String id){
        if(!id.equals("")){
            preferences.edit().putString("id", id).apply();

            Log.d(TAG, "set ID: " + id);
        }
        id = preferences.getString("id", null);
        idTextView.setText("User ID: " + id);
    }

    public void register(String token){
        String newUserId = preferences.getString("id", null);
        String noticeUserId = noticeUserEditText.getText().toString();
        String pushId = preferences.getString("token", null);
        if(pushId == null){
            getFirebaseToken();
            pushId = preferences.getString("token", null);
        }

        String post = "apiType=register" + "&newUserId=" + newUserId + "&noticeUserId=" + noticeUserId + "&pushId=" + pushId;
        HTTPSUtility https = new HTTPSUtility();
        https.execute("https://hacku.dragon-egg.org/api", post);
    }

    public void getFirebaseToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d(TAG, "Firebase token: " + token);

                        //Preferencesに自トークンを記録
                        preferences.edit().putString("token", token).apply();
                    }
                });

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
