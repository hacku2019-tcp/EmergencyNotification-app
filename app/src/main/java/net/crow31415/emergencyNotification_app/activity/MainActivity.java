package net.crow31415.emergencyNotification_app.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import net.crow31415.emergencyNotification_app.R;
import net.crow31415.emergencyNotification_app.service.AccelerationMeasureService;

public class MainActivity extends AppCompatActivity {

    private final MainActivity self = this;
    private FirebaseAnalytics mAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAnalytics = FirebaseAnalytics.getInstance(self);

        Button startButton = findViewById(R.id.button_start);
        Button stopButton = findViewById(R.id.button_stop);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // サービス開始
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(getApplication(), AccelerationMeasureService.class);
                if(Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(serviceIntent);
                }else{
                    startService(serviceIntent);
                }
            }
        });

        // サービス停止
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(getApplication(), AccelerationMeasureService.class);
                stopService(serviceIntent);
            }
        });
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
