package kobbigal.weatherservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import kobbigal.monitorservice.R;

/**
 * Created by kobbigal on 11/25/17.
 */

public class MonitorService extends Service {

    final int NOTIF_ID = 1;
    NotificationManager notificationManager;
    Notification.Builder builder;
    Notification notification;
    SensorManager sensorManager;
    Sensor temperature;

    @Override
    public void onCreate() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_memory_white_24dp)
                .setContentTitle("System Monitor");

        notification = builder.build();
        startForeground(NOTIF_ID, notification);

//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        sensorManager.registerListener(MonitorService.this, temperature, 10000000);
//        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




//
//        sensorManager.registerListener(new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//
//
//
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//            }
//        });



        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                assert activityManager != null;
                activityManager.getMemoryInfo(mi);

                double availableMegs = mi.availMem / 0x100000L;
                double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;


                Log.i("available", String.valueOf(availableMegs) + "MB");
                Log.i("percent", String.valueOf(percentAvail) + "%");

                String messageBody = (int) availableMegs + "MB \\ " + (int) percentAvail + "%";

                builder
                        .setContentText(messageBody)
                        .setStyle(new Notification.BigTextStyle().bigText(messageBody));
                notificationManager.notify(NOTIF_ID, builder.build());

            }
        }, 0, 10000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
