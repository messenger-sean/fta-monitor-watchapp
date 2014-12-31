package com.fsilberberg.ftamonitor.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import com.fsilberberg.ftamonitor.R;
import com.fsilberberg.ftamonitor.common.Observer;
import com.fsilberberg.ftamonitor.view.DrawerActivity;
import microsoft.aspnet.signalr.client.ConnectionState;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This service is the main background service that runs all of the other service threads and
 * holds a wakelock. It is responsible for managing the lifecycle of all of the other "services"
 * that communicate with the field, set notifications, and keep field time.
 */
public class MainForegroundService extends Service implements Observer<ConnectionState> {

    // Public intent extras for communicating with this service
    public static final String CLOSE_CONNECTION_INTENT_EXTRA = "CLOSE_CONNECTION_INTENT_EXTRA";

    // Notification ID for updating the ongoing notification. 3 has no special significance other
    // than being my favorite number
    private static final int ID = 3;
    private static final int MAIN_ACTIVITY_INTENT_ID = 1;
    private static final int CLOSE_SERVICE_INTENT_ID = 2;

    // Other "services" managed by this service
    private final Collection<ForegroundService> m_services;
    private final FieldConnectionService m_fieldService;
    private PowerManager.WakeLock m_wl;

    public MainForegroundService() {
        m_services = new ArrayList<>();
        // Add all services to the list
        m_fieldService = new FieldConnectionService();
        m_services.add(m_fieldService);
        m_services.add(new FieldProblemNotificationService());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // First, check if we should stop the service.
        if (intent.getBooleanExtra(CLOSE_CONNECTION_INTENT_EXTRA, false)) {
            FieldConnectionService.deregisterConnectionObserver(this);
            for (ForegroundService service : m_services) {
                service.stopService();
            }
            stopForeground(false);
            stopSelf();
            return START_REDELIVER_INTENT;
        }

        if (m_wl == null || !m_wl.isHeld()) {
            // Aquire a wakelock. While the application is running, we will always hold a wakelock
            // to ensure communication with the field
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            m_wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FTA Monitor Service");
        }

        for (ForegroundService service : m_services) {
            service.startService(this, intent);
        }

        // Start the service in the foreground
        startForeground(ID, createNotification(FieldConnectionService.getState()));

        // Register this as a connection state observer
        FieldConnectionService.registerConnectionObserver(this);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void update(ConnectionState updateType) {
        startForeground(ID, createNotification(updateType));
    }

    private Notification createNotification(ConnectionState state) {
        String contentText = state.toString() + " - " + m_fieldService.getUrl();

        // Create the intent for the main action
        Intent mainIntent = new Intent(this, DrawerActivity.class);
        mainIntent.putExtra(DrawerActivity.VIEW_INTENT_EXTRA, DrawerActivity.DisplayView.FIELD_MONITOR.ordinal());

        // Create the intent for the action button
        Intent actionIntent = new Intent(this, MainForegroundService.class);
        actionIntent.putExtra(CLOSE_CONNECTION_INTENT_EXTRA, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Field Monitor")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(
                        PendingIntent.getActivity(this, MAIN_ACTIVITY_INTENT_ID, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .addAction(R.drawable.ic_action_remove, "Close Monitor",
                        PendingIntent.getService(this, CLOSE_SERVICE_INTENT_ID, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        return builder.build();
    }
}
