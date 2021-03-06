package euphoria.psycho.knife.server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.nanohttpd.protocols.http.NanoHTTPD;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.StorageUtils;
import euphoria.psycho.share.util.NotificationUtils;
import euphoria.psycho.share.util.SystemUtils;
import euphoria.psycho.share.util.ZipUtils;

public class FileServer extends Service {
    public static final String DEFAULT_CHANNEL_NAME = "File Server";
    public static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CHANNEL_ID = "server";
    private static final String DEFAULT_STATIC_DIRECTORY = "static";
    private static final String DEFAULT_UPLOAD_DIRECTORY = "upload";
    private NotificationManager mManager;
    private static final int FOREGROUND_ID = 1;
    WebServer mWebServer;
    LocalBinder mLocalBinder = new LocalBinder();


    private void startServer() {
        if (mWebServer != null) return;

//        Log.e("TAG/FileServer", "startServer: " + SystemUtils.getDeviceIP(this));
//        ThreadUtils.postOnBackgroundThread(() -> {
//            String localIp = ServerUtils.getLocalIp();
//
//            Log.e("TAG/FileServer", "run: " + localIp);
//
//        });
        String ip = SystemUtils.getDeviceIP(this);
        if (ip == null) {
            ip = ServerUtils.getLocalIp();
        }
        mWebServer = new WebServer(ip, DEFAULT_PORT);
        File staticDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_STATIC_DIRECTORY);

        if (!staticDirectory.isDirectory()) {
            staticDirectory.mkdirs();
        }

        unpackAssets(staticDirectory);
        mWebServer.setStaticDirectory(staticDirectory);
        mWebServer.setUploadDirectory(createUploadDirectory());
        mWebServer.setStartDirectory(Environment.getExternalStorageDirectory());
        try {
            mWebServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        } catch (IOException e) {

            Log.e("TAG/FileServer", "startServer: " + e.getMessage());

        }

    }

    private String mTreeUri;

    private File createUploadDirectory() {
        File uploadDirectory;
        // mTreeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);
        if (mTreeUri != null) {
            uploadDirectory = new File(StorageUtils.getSDCardPath(), DEFAULT_UPLOAD_DIRECTORY);
            if (!uploadDirectory.isDirectory()) {
                StorageUtils.createDirectory(this, new File(StorageUtils.getSDCardPath()),
                        DEFAULT_UPLOAD_DIRECTORY, FileUtils.getTreeUri().toString());
            }
        } else {
            uploadDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_UPLOAD_DIRECTORY);
            if (!uploadDirectory.isDirectory()) {
                uploadDirectory.mkdirs();
            }
        }

        return uploadDirectory;
    }

    private void unpackAssets(File dir) {


        AssetManager assetManager = getAssets();
        try (InputStream is = assetManager.open("static/static.zip")) {
            ZipUtils.exactInputStream(is, dir);
        } catch (Exception e) {
            e.printStackTrace();

            Log.e("TAG/FileServer", "unpackAssets: " + e.getMessage());

        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationUtils.createNotificationChannel(mManager, DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);

        Notification notification = NotificationUtils.createNotification(this, DEFAULT_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_phonelink_blue_24px)
                .build();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        public String getServerURL() {
            return mWebServer.getURL();
        }


    }


}
