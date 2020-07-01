package com.fullstackoasis.sensordemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getCanonicalName();
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean sensorExists;
    private int ACCELEROMETER_DOES_NOT_EXIST = 2;
    private static final int INTERVAL = 1000;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAccelerometerSensorService();
        Button btn = findViewById(R.id.btnViewGraph);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Should we bother with this task if there's no bluetooth?
        // The step counter can in fact work independently of bluetooth.
        handler = new Handler();
        startRepeatingTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopRepeatingTask();
    }

    private void startAccelerometerSensorService() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Log.d(TAG, "sensorManager is NOT null");
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.d(TAG, "" + accelerometerSensor );
            sensorExists = (accelerometerSensor != null);
            List<Sensor> l = sensorManager.getSensorList(Sensor.TYPE_ALL);
            Log.d(TAG, "length of sensor list is " + l.size());
            Log.d(TAG, "Sensor.TYPE_ACCELEROMETER is " + Sensor.TYPE_ACCELEROMETER);
            for (Sensor s : l) {
                String name = s.getName();
                String stype = s.getStringType();
                int type = s.getType();
                Log.d(TAG,
                        "name of sensor in list is " + name + ", stype = " + stype + ", type = " + type);
            }
        }
        if (!sensorExists) {
            Log.d(TAG, "sensorExists is false");
            /** Do not bother starting this service. There is no sensor */
            handleError(ACCELEROMETER_DOES_NOT_EXIST);
            return;
        }
        Intent i = new Intent(this, AccelerometerSensorService.class);
        Log.d(TAG, "Going to start the accelerometer service");
        startService(i);
    }

    private void handleError(int code) {
        // ignore code, for now.
        TextView tv = findViewById(R.id.tvErrorAccelerometerNotFound);
        tv.setText(getResources().getString(R.string.accelerometer_not_found));
    }
    Runnable updater = new Runnable() {
        @Override
        public void run() {
            try {
                updateExistingMessage(); //this function can change value of mInterval.
            } finally {
                // Make sure this happens, even if exception is thrown
                handler.postDelayed(updater, INTERVAL);
            }
        }
    };

    private void updateExistingMessage() {
        TextView tv = findViewById(R.id.tvErrorAccelerometerNotFound);
        String template = getResources().getString(R.string.display_data);
        String s = String.format(template, AccelerometerSensorService.CURRENT_X,
                AccelerometerSensorService.CURRENT_Y, AccelerometerSensorService.CURRENT_Z);
        tv.setText(s);
    }

    void startRepeatingTask() {
        updater.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(updater);
    }

}
