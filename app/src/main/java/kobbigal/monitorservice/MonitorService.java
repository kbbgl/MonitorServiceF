package kobbigal.monitorservice;

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
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
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
//    SensorManager sensorManager;
//    Sensor temperature;
    TelephonyManager telephonyManager;
    mPhoneStateListener mPhoneStateListener;
    int mSignalStrength;

    @Override
    public void onCreate() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new mPhoneStateListener();
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

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

        Log.i("onstartcommand", "y");
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


                String messageBody =
                        "Free RAM: " + (int) availableMegs + "MB \\ " + (int) percentAvail + " %\n"
                        + "GSM Signal: " + mPhoneStateListener.getRssi() + " rssi \\ "
                        + mPhoneStateListener.getDbm() + " dbm \\ "
                        + mPhoneStateListener.getQuality();

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

class mPhoneStateListener extends PhoneStateListener {

    private int rssi;
    private int dbm;
    private String quality;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        int mSignalStrength = signalStrength.getGsmSignalStrength();
        int mSignalStrengthDbm = (2 * mSignalStrength) - 113;

        Log.i("dbm", String.valueOf(mSignalStrengthDbm));
        Log.i("rssi", String.valueOf(mSignalStrength));

        rssi = mSignalStrength;
        dbm = mSignalStrengthDbm;
    }

    public int getDbm() {
        return dbm;
    }


    int getRssi() {
        return rssi;
    }

    String getQuality() {

        Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + rssi);

        if (rssi > 30) {
            quality = "Good";

        } else if (rssi > 20 && rssi < 30) {
            quality = "Average";

        } else if (rssi < 20 && rssi > 3) {
            quality = "Weak";

        } else if (rssi < 3) {
            quality = "Very weak";
        }

        Log.d(getClass().getCanonicalName(), quality );

        return quality;
    }
    /*
     @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        signalSupport = signalStrength.getGsmSignalStrength();
        Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + rssi);

        if (rssi > 30) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Good");


        } else if (rssi > 20 && rssi < 30) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Avarage");


        } else if (rssi < 20 && rssi > 3) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Week");


        } else if (rssi < 3) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Very week");
        }
     */
}
