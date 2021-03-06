package euphoria.psycho.knife.service;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.PluralsRes;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.MainActivity;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.service.FileOperationService.OpType;

import static euphoria.psycho.knife.service.FileOperationService.EXTRA_CANCEL;
import static euphoria.psycho.knife.service.FileOperationService.EXTRA_FAILED_DOCS;
import static euphoria.psycho.knife.service.FileOperationService.EXTRA_JOB_ID;
import static euphoria.psycho.knife.service.FileOperationService.EXTRA_OPERATION_TYPE;

public abstract class Job implements Runnable {
    static final String INTENT_TAG_CANCEL = "cancel";
    static final String INTENT_TAG_FAILURE = "failure";
    static final String INTENT_TAG_PROGRESS = "progress";
    static final int STATE_CANCELED = 4;
    static final int STATE_COMPLETED = 3;
    static final int STATE_CREATED = 0;
    static final int STATE_SET_UP = 2;
    static final int STATE_STARTED = 1;
    private static final String TAG = "TAG/" + Job.class.getSimpleName();
    final Listener listener;
    final Context mAppContext;
    final Context mContext;
    final ArrayList<String> mFailedDocs = new ArrayList<>();
    final String mId;
    private final @OpType
    int mOpType;
    final Notification.Builder mProgressBuilder;
    final CancellationSignal mSignal = new CancellationSignal();
    private int mFailureCount;
    private volatile @State
    int mState = STATE_CREATED;

    public Job(Context context, String id, Listener listener, @OpType int opType) {
        mContext = context;
        mAppContext = context.getApplicationContext();
        mId = id;
        mOpType = opType;
        this.listener = listener;
        mProgressBuilder = createProgressBuilder();
    }

    Intent buildNavigateIntent(String tag) {
        // TODO (b/35721285): Reuse an existing task rather than creating a new one every time.
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    final void cancel() {
        mState = STATE_CANCELED;
        mSignal.cancel();
    }

    final void cleanup() {

    }

    Intent createCancelIntent() {
        final Intent cancelIntent = new Intent(mContext, FileOperationService.class);
        cancelIntent.setData(getDataUriForIntent(INTENT_TAG_CANCEL));
        cancelIntent.putExtra(EXTRA_CANCEL, true);
        cancelIntent.putExtra(EXTRA_JOB_ID, mId);
        return cancelIntent;
    }

    Builder createNotificationBuilder() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            return new Builder(mContext, FileOperationService.NOTIFICATION_CHANNEL_ID);
        } else {

            return new Notification.Builder(mContext);
        }
    }

    final Builder createProgressBuilder(
            String title, @DrawableRes int icon,
            String actionTitle, @DrawableRes int actionIcon) {
        Builder progressBuilder = createNotificationBuilder()
                .setContentTitle(title)
                .setContentIntent(
                        PendingIntent.getActivity(mAppContext, 0,
                                buildNavigateIntent(INTENT_TAG_PROGRESS), 0))

                .setSmallIcon(icon)
                .setOngoing(true);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            progressBuilder.setCategory(Notification.CATEGORY_PROGRESS);
        }

        final Intent cancelIntent = createCancelIntent();
        progressBuilder.addAction(
                actionIcon,
                actionTitle,
                PendingIntent.getService(
                        mContext
                        ,
                        0,
                        cancelIntent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));

        return progressBuilder;
    }

    abstract Builder createProgressBuilder();

    abstract void finish();

    Uri getDataUriForIntent(String tag) {
        return Uri.parse(String.format("data,%s-%s", tag, mId));
    }

    public ArrayList<String> getFailedDocs() {
        return mFailedDocs;
    }

    Notification getFailureNotification(@PluralsRes int titleId, @DrawableRes int icon) {
        final Intent navigateIntent = buildNavigateIntent(INTENT_TAG_FAILURE);
        //navigateIntent.putExtra(EXTRA_DIALOG_TYPE, OperationDialogFragment.DIALOG_TYPE_FAILURE);
        navigateIntent.putExtra(EXTRA_OPERATION_TYPE, mOpType);
        navigateIntent.putExtra(EXTRA_FAILED_DOCS, mFailedDocs);

        final Builder errorBuilder = createNotificationBuilder()
                .setContentTitle(mContext.getResources().getQuantityString(titleId,
                        mFailureCount, mFailureCount))
                .setContentText(mContext.getString(R.string.notification_touch_for_details))
                .setContentIntent(PendingIntent.getActivity(mAppContext, 0, navigateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))

                .setSmallIcon(icon)
                .setAutoCancel(true);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            errorBuilder.setCategory(Notification.CATEGORY_ERROR);
        }
        return errorBuilder.build();
    }

    abstract Notification getFailureNotification();

    public String getId() {
        return mId;
    }

    abstract Notification getProgressNotification();

    abstract Notification getSetupNotification();

    Notification getSetupNotification(String content) {
        mProgressBuilder.setProgress(0, 0, true)
                .setContentText(content);
        return mProgressBuilder.build();
    }

    final @State
    int getState() {
        return mState;
    }

    abstract Notification getWarningNotification();

    final boolean hasFailures() {
        return mFailureCount > 0;
    }

    boolean hasWarnings() {
        return false;
    }

    final boolean isCanceled() {
        return mState == STATE_CANCELED;
    }

    final boolean isFinished() {
        return mState == STATE_CANCELED || mState == STATE_COMPLETED;
    }

    boolean setUp() {
        return true;
    }

    abstract void start();

    @Override
    public void run() {
        if (isCanceled()) {
            // Canceled before running
            return;
        }

        mState = STATE_STARTED;
        listener.onStart(this);

        try {
            boolean result = setUp();
            if (result && !isCanceled()) {
                mState = STATE_SET_UP;
                start();
            }
        } catch (RuntimeException e) {
            // No exceptions should be thrown here, as all calls to the provider must be
            // handled within Job implementations. However, just in case catch them here.
            Log.e(TAG, "Operation failed due to an unhandled runtime exception.", e);
        } finally {
            mState = (mState == STATE_STARTED || mState == STATE_SET_UP) ? STATE_COMPLETED : mState;
            finish();
            listener.onFinished(this);


        }

    }

    interface Listener {
        void onFinished(Job job);

        void onStart(Job job);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_CREATED, STATE_STARTED, STATE_SET_UP, STATE_COMPLETED, STATE_CANCELED})
    @interface State {
    }
}
