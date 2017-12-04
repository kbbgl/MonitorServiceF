package kobbigal.monitorservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

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
    TelephonyManager telephonyManager;
    mPhoneStateListener mPhoneStateListener;
    TextView ram;
    TextView gsm;
    TextView temp;
    TextView bat;
    RemoteViews contentView ;
    Timer timer;

    @Override
    public void onCreate() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new mPhoneStateListener();
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);

        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_background).setContent(contentView);
//                .setContentTitle("Monitoring Service");

        notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(NOTIF_ID, notification);
        startForeground(NOTIF_ID, notification);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("onstartcommand", "y");

        timer = new Timer();
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

                contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);

                /*
                String messageBody =
                        "Free RAM: " + (int) availableMegs + "MB \\ " + (int) percentAvail + " %\n"
                        + "GSM Signal: " + mPhoneStateListener.getRssi() + " rssi \\ "
                        + mPhoneStateListener.getDbm() + " dbm \\ "
                        + mPhoneStateListener.getQuality();

                builder
                        .setContentText(messageBody)
                        .setStyle(new Notification.BigTextStyle().bigText(messageBody));
                */
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

        timer.cancel();
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

    int getDbm() {
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
}
