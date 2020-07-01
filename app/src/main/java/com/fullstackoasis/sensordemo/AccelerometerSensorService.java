package com.fullstackoasis.sensordemo;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * This is not intended to be a "bound service".
 * https://developer.android.com/guide/components/services#Types-of-services
 */
public class AccelerometerSensorService extends Service implements SensorEventListener {
    private static String TAG = AccelerometerSensorService.class.getCanonicalName();
    public static final String ID_CHANNEL = "AcceleromrterChannel";
    private static final int ONGOING_NOTIFICATION_ID = 29;
    public static final int CHANGED = 1;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private int startId;
    // This debug flag is only here for testing to see if this service has been started as a
    // foreground service. If so, a log message is printed periodically.
    private static boolean DEBUG = false;
    private Handler handler;
    public static float CURRENT_X = 0;
    public static float CURRENT_Y = 0;
    public static float CURRENT_Z = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AccelerometerSensorService.onCreate");
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            // Maybe the device does not have this sensor. If not, do not try to use it!
            Log.d(TAG, "AccelerometerSensorService.onCreate sensorManager was found to be null");
            return;
        }
        Log.d(TAG, "AccelerometerSensorService.onCreate sensorManager is not null");
        List<Sensor> l = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "AccelerometerSensorService.onCreate length of sensor list is " + l.size());
        for (Sensor s : l) {
            String name = s.getName();
            Log.d(TAG, "AccelerometerSensorService.onCreate name of sensor in list is " + name);
        }
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            Log.d(TAG, "AccelerometerSensorService.onCreate accelerometerSensor was NOT null");
            sensorManager.registerListener(this, accelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d(TAG, "AccelerometerSensorService.onCreate accelerometerSensor IS null");
        }
        // For debug only
        if (DEBUG) {
            handler = new Handler();
            startRepeatingTask();
        }
    }

    /**
     * WARNING! ONLY USE THIS IN DEBUG MODE.
     */
    void startRepeatingTask() {
        updater.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(updater);
    }

    /**
     * WARNING! ONLY USE THIS IN DEBUG MODE.
     */
    Runnable updater = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG,
                        "is service running in foreground?? " + isServiceRunningInForeground(AccelerometerSensorService.this,
                                AccelerometerSensorService.class));
                handleSensorChanged(null);
            } finally {
                // Make sure this happens, even if exception is thrown
                handler.postDelayed(updater, 5000);
            }
        }
    };

    private int getNotificationIcon() {
        return R.drawable.logo_accelerometer_project;
    }

    /**
     * Get the PendingIntent that will open the app's home screen when a user clicks on the
     * Notification.
     * @return PendingIntent for home screen (MainActivity)
     */
    private PendingIntent getPendingIntentToMainActivity() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        return pendingIntent;
    }

    /**
     * This method does two things: (1) it starts the app as a foreground service in Android >
     * Build.VERSION_CODES.O. Also, it emits a notification. I am not 100% sure the notification
     * is needed, and it may be removed.
     * @param intent
     * @param flags
     * @param startId
     */
    private void notifyStartForeground(Intent intent, int flags, int startId) {
        Log.d(TAG, "is service running in foreground?? " + isServiceRunningInForeground(this,
                AccelerometerSensorService.class));
        Log.d(TAG,
                "Build.VERSION.SDK_INT " + Build.VERSION.SDK_INT + ", Build.VERSION_CODES.O "+ Build.VERSION_CODES.O );

        PendingIntent pendingIntent =  getPendingIntentToMainActivity();//PendingIntent
        // .getActivity(this, 0, intent, 0);

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = null;
        // The id of the channel.

        int importance;

        // Dev note: You must create Builder with a non-null idChannel, otherwise notification
        // does not happen.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ID_CHANNEL);
        builder.setContentTitle(this.getString(R.string.app_name))
                .setSmallIcon(getNotificationIcon())
                .setContentIntent(pendingIntent)
                .setColorized(false)
                .setContentText(getString(R.string.notification_message));
        int color = ContextCompat.getColor(this, R.color.colorAccent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // https://github.com/android/user-interface-samples/tree/master/Notifications
            // Notification Channels arrived in SDK 29.
            // VERSION_CODES.O is 26.
            // "Notification Channel Id is ignored for Android pre O (26)."

            Log.d(TAG, "Here");
            // With SDK 26 and after, use IMPORTANCE_HIGH, setImportance, not setPriority
            // Prior to SDK 26, use Notification.PRIORITY_HIGH for example.
            // Says use at least PRIORITY_LOW for foreground apps.
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            mChannel = new NotificationChannel(ID_CHANNEL, getString(R.string.app_name), importance);
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.notification_message));
            mChannel.enableLights(true);
            mChannel.setShowBadge(true);
            mChannel.setLightColor(color);
            mChannel.setVibrationPattern(new long[]{100});
            try {
                mNotificationManager.createNotificationChannel(mChannel);
                //startForegroundService(intent);
                Notification notification = builder.build();
                // DO NOT DO THIS! You wind up with a double bling sound, which is overkill.
                //mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
                startForeground(ONGOING_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.d(TAG, "There");
            // Prior to SDK 26, use Notification.PRIORITY_HIGH for example.
            // Says use at least PRIORITY_LOW for foreground apps.
            importance = Notification.PRIORITY_DEFAULT;
            // https://developer.android.com/reference/android/app/Notification.Builder#setPriority
            builder.setPriority(importance)
                    .setColor(color)
                    .setVibrate(new long[]{100})
                    .setLights(Color.YELLOW, 500, 5000)
                    .setAutoCancel(true);
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AccelerometerSensorService.onStartCommand");
        Log.d(TAG, "onStartCommand got startId " + startId);
        Log.d(TAG, "onStartCommand got intent action " + intent.getAction());
        Log.d(TAG, "onStartCommand got intent flags " + flags);
        this.startId = startId;
        if (!isServiceRunningInForeground(AccelerometerSensorService.this,
                AccelerometerSensorService.class)) {
            notifyStartForeground(intent, flags, startId);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (accelerometerSensor != null && sensorManager != null) {
            sensorManager.unregisterListener(this, accelerometerSensor);
        }
        if (DEBUG) {
            stopRepeatingTask();
        }
    }

    /**
     * Do not allow binding, always return null.
     * "You must always implement this method; however, if you don't want to allow binding, you
     * should return null."
     * https://developer.android.com/guide/components/services#Basics
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This private method is used by onSensorChanged AND by any test methods or debug methods.
     */
    private void handleSensorChanged(SensorEvent event) {
        int currentSteps = 0;
        int totalSteps = 0;
        if (event != null) {
            for (float f : event.values) {
                Log.d(TAG, "" + f);
            }
            CURRENT_X = event.values[0];
            CURRENT_Y = event.values[1];
            CURRENT_Z = event.values[2];
        }
        Log.d(TAG,
                "** handledChanged, currentSteps and totalSteps ** " + currentSteps + ", " + totalSteps);
        if (currentSteps < totalSteps) {
            // TODO FIXME. The sensor is attempting to update db even when there's no current
            //  challenge. You probably do not want this service to run if the user is not
            //  involved in a challenge. So, fix later.

            // TODO FIXME. This is important business logic. If it is detected that we have
            //  completed the last step in the challenge, then update the challenge to completed.
            //  Then the challenge will no longer get updated.
            int newSteps = currentSteps + 1;
            Log.d(TAG,"** handledChanged, got into here.... ** ");
            if (newSteps >= totalSteps) {
                Log.d(TAG, "** You are going to run updateCurrentChallengeCompleted **");
                // See https://stackoverflow.com/questions/22485298/stopself-vs-stopselfint-vs-stopserviceintent
                // the idea is to avoid crashes by only stopping if the start id is same as
                // one that started this service using onStartCommand.
                // Calling stopSelf should trigger onDestroy, which unregisters service.
                stopSelf(startId);
            } else {
                Log.d(TAG,"** handledChanged, simple update ** ");
                // TODO FIXME. This updates the repo, AND it does notify the UI because it's the same
                //  repo with the same backing Room database.
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Sensor changes when user takes just 1 detected step.
        handleSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO FIXME
    }
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }
}