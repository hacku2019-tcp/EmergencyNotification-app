package net.crow31415.emergencyNotification_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.util.HTTPSUtility;

public class SendNotificationActivity extends AppCompatActivity {

    private String TAG;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        TAG = getResources().getString(R.string.app_name);
        Log.d(TAG, "called SendNotificationActivity.onCreate()");

        preferences = getSharedPreferences("net.crow31415.emergencyNotification_app.preferences", MODE_PRIVATE);
        sendNotification();
    }

    public void sendNotification(){
        Log.d(TAG, "called SendNotificationActivity.sendNotification()");
        Log.i(TAG, "Notification was sent.");

        String userId = preferences.getString("id", null);

        String post = "{" +
                "\"apiType\" : \"notification\", " +
                "\"userId\" : \"" + userId + "\"" +
                "}";
        HTTPSUtility https = new HTTPSUtility();
        https.execute("https://hacku.dragon-egg.org/api", post);
    }
}
