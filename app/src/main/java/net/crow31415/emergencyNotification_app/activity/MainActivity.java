package net.crow31415.emergencyNotification_app.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;
import net.crow31415.emergencyNotification_app.util.HTTPSUtility;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final MainActivity self = this;
    private FirebaseAnalytics mAnalytics;
    private TextView idTextView;
    private EditText idEditText;
    private Button registerIdButton;
    private EditText noticeUserEditText;
    private Button registerNotificationButton;
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
        idTextView = findViewById(R.id.id_text_view);
        idEditText = findViewById(R.id.id_edit_text);
        registerIdButton = findViewById(R.id.button_register_id);
        noticeUserEditText = findViewById(R.id.notice_user_edit_text);
        registerNotificationButton = findViewById(R.id.button_register_user);
        serviceButton = findViewById(R.id.button_toggle_service);
        preferences = getSharedPreferences("net.crow31415.emergencyNotification_app.preferences", MODE_PRIVATE);

        initID();
        checkServiceRunning();
        getFirebaseToken();

        registerIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerId(idEditText.getText().toString());
            }
        });

        registerNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNotification(noticeUserEditText.getText().toString());
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
        applyID();
    }

    private void applyID(){
        String id = preferences.getString("id", null);
        if(id == null) {
            id = "";
        }
        idTextView.setText("User ID: " + id);
    }

    private void registerId(String id){
        if(!id.equals("")){
            preferences.edit().putString("id", id).apply();

            Log.d(TAG, "register ID: " + id);
        }

        //call API
        String userId = preferences.getString("id", null);
        String pushId = preferences.getString("token", null);
        /*
        if(pushId == null){
            getFirebaseToken();
            pushId = preferences.getString("token", null);

            while (pushId == null){
                pushId = preferences.getString("token", null);
            }
            Log.d(TAG, "token: " + pushId);
        }

         */

        String post = "{" +
                "\"apiType\" : \"registerToken\", " +
                "\"userId\" : \"" + userId + "\", " +
                "\"pushId\" : \"" + pushId + "\"" +
                "}";
        HTTPSUtility https = new HTTPSUtility();
        https.execute("https://hacku.dragon-egg.org/api", post);

        applyID();
    }

    public void registerNotification(String notificationId){
        String userId = preferences.getString("id", null);

        String post = "{" +
                "\"apiType\" : \"registerNotification\", " +
                "\"userId\" : \"" + userId + "\", " +
                "\"notificationId\" : \"" + notificationId + "\"" +
                "}";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_oss) {
            Intent intent = new Intent(this, OssLicensesMenuActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra("title", getString(R.string.action_oss));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
