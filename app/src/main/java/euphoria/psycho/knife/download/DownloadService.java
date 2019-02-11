package euphoria.psycho.knife.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.Nullable;


public class DownloadService extends Service {

    public static final String EXTRA_CANCEL = "cancel";
    private static final String TAG = "TAG/" + DownloadService.class.getSimpleName();
    protected NotificationManager mNotificationManager;
    protected ForegroundManager mForegroundManager;
    protected int mLastServiceId;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    protected void handleCancel(Intent intent) {

    }

    protected void handleIntent(Intent intent) {
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }
        mWakeLock.acquire();
    }

    private String provideChannelId() {

        return "default";
    }

    protected String provideChannelName() {
        return "Download";
    }

    protected int provideImportance() {
        return NotificationManager.IMPORTANCE_LOW;
    }

    protected void setUpNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    provideChannelId(),
                    provideChannelId(),
                    provideImportance()
            );
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private static ForegroundManager createForegroundManager(final Service service) {
        return new ForegroundManager() {
            @Override
            public void startForeground(int id, Notification notification) {
                service.startForeground(id, notification);
            }

            @Override
            public void stopForeground(boolean removeNotification) {
                service.stopForeground(removeNotification);
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mForegroundManager == null) {
            mForegroundManager = createForegroundManager(this);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        setUpNotificationChannel();
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(EXTRA_CANCEL)) {
            handleCancel(intent);
        } else {

            handleIntent(intent);
        }
        mLastServiceId = startId;

        return START_NOT_STICKY;
    }

    interface ForegroundManager {
        void startForeground(int id, Notification notification);

        void stopForeground(boolean removeNotification);
    }
}
