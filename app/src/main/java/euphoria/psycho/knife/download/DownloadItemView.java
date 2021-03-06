package euphoria.psycho.knife.download;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MarginLayoutParamsCompat;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.ViewUtils;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.ListMenuButton.Item;
import euphoria.psycho.common.widget.MaterialProgressBar;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProvider;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailRequest;
import euphoria.psycho.share.util.ThreadUtils;
import euphoria.psycho.share.util.Utils;

public class DownloadItemView extends SelectableItemView<DownloadInfo> implements ThumbnailRequest, ListMenuButton.Delegate {
    private final ColorStateList mCheckedIconForegroundColorList;
    private final int mIconBackgroundResId;
    private final ColorStateList mIconForegroundColorList;
    private final int mMargin;
    private final int mMarginSubsection;
    private ImageButton mPauseResumeButton;
    private int mIconSize;
    private LinearLayout mLayoutContainer;
    private ListMenuButton mMoreButton;
    private MaterialProgressBar mProgressView;
    private TextView mDescriptionCompletedView;
    private TextView mDownloadPercentageView;
    private TextView mDownloadStatusView;
    private TextView mFilenameCompletedView;
    private TextView mFilenameInProgressView;
    private View mCancelButton;
    private View mLayoutCompleted;
    private View mLayoutInProgress;
    private Bitmap mThumbnailBitmap;

    public DownloadItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMargin = context.getResources().getDimensionPixelSize(R.dimen.list_item_default_margin);
        mMarginSubsection =
                context.getResources().getDimensionPixelSize(R.dimen.list_item_subsection_margin);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.list_item_start_icon_width);
        mCheckedIconForegroundColorList = AppCompatResources.getColorStateList(context, R.color.white_mode_tint);
        mIconBackgroundResId = R.drawable.list_item_icon_modern_bg;
        mIconForegroundColorList =
                AppCompatResources.getColorStateList(context, R.color.dark_mode_tint);
    }

    public void displayItem(DownloadInfo item) {
        updateView();
        setItem(item);

        MarginLayoutParamsCompat.setMarginStart(
                (MarginLayoutParams) mLayoutContainer.getLayoutParams(), mMargin);

        ThumbnailProvider thumbnailProvider = DownloadManager.instance().provideThumbnailProvider();
        thumbnailProvider.cancelRetrieval(this);

        // Request a thumbnail for the file to be sent to the ThumbnailCallback. This will happen
        // immediately if the thumbnail is cached or asynchronously if it has to be fetched from a
        // remote source.
        mThumbnailBitmap = null;

        if (item.status == DownloadStatus.COMPLETED) {
            thumbnailProvider.getThumbnail(this);
        }
        if (mThumbnailBitmap == null) updateView();

        Context context = mDescriptionCompletedView.getContext();
        mFilenameCompletedView.setText(item.fileName);
        mFilenameInProgressView.setText(item.fileName);

        String description = context.getString(R.string.download_manager_list_item_description,
                Formatter.formatFileSize(getContext(), item.getFileSize()),
                item.getDisplayName());
        mDescriptionCompletedView.setText(description);

        if (item.isComplete()) {
            showLayout(mLayoutCompleted);

            // To ensure that text views have correct width after recycling, we have to request
            // re-layout.
            mFilenameCompletedView.requestLayout();
        } else {
            showLayout(mLayoutInProgress);
            mDownloadStatusView.setText(item.getStatusString(getContext()));

            //Progress progress = item.getDownloadProgress();

            if (item.isPaused()) {
                mPauseResumeButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_resume_button));
            } else {
                mPauseResumeButton.setImageResource(R.drawable.ic_pause_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_pause_button));
            }

            mProgressView.setProgress(item.getPercent());
            mDownloadPercentageView.setText(item.getPercent() + "%");
            MarginLayoutParamsCompat.setMarginEnd(
                    (MarginLayoutParams) mDownloadPercentageView.getLayoutParams(), mMargin);
        }

        mMoreButton.setContentDescriptionContext(item.fileName);
        boolean canShowMore = item.isComplete();
        mMoreButton.setVisibility(canShowMore ? View.VISIBLE : View.GONE);
        mMoreButton.setClickable(true);

        setLongClickable(item.isComplete());

    }

    private void setThumbnailBitmap(Bitmap thumbnail) {
        mThumbnailBitmap = thumbnail;
        updateView();
    }

    private void showLayout(View layoutToShow) {
        if (mLayoutCompleted != layoutToShow) ViewUtils.removeViewFromParent(mLayoutCompleted);
        if (mLayoutInProgress != layoutToShow) ViewUtils.removeViewFromParent(mLayoutInProgress);

        if (layoutToShow.getParent() == null) {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            mLayoutContainer.addView(layoutToShow, params);

            // Move the menu button to the back of mLayoutContainer.
            mLayoutContainer.removeView(mMoreButton);
            mLayoutContainer.addView(mMoreButton);
        }
    }

    public void updateProgress(DownloadInfo downloadInfo) {

        mDownloadStatusView.setText(downloadInfo.getStatusString(getContext()));
        switch (downloadInfo.status) {
            case DownloadStatus.PENDING:
            case DownloadStatus.STARTED: {
                mPauseResumeButton.setImageResource(R.drawable.ic_pause_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_pause_button));
                break;
            }
            case DownloadStatus.IN_PROGRESS: {

                mProgressView.setProgress(downloadInfo.getPercent());
                mDownloadPercentageView.setText(downloadInfo.getPercent() + "%");
                break;
            }
            case DownloadStatus.PAUSED: {
                mPauseResumeButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_resume_button));

                break;
            }
            case DownloadStatus.COMPLETED: {
                String description = getContext().getString(R.string.download_manager_list_item_description,
                        Formatter.formatFileSize(getContext(),
                                downloadInfo.getFileSize()),
                        downloadInfo.getDisplayName());
                mDescriptionCompletedView.setText(description);
                showLayout(mLayoutCompleted);

                // To ensure that text views have correct width after recycling, we have to request
                // re-layout.
                mFilenameCompletedView.requestLayout();

                mMoreButton.setContentDescriptionContext(downloadInfo.fileName);
                boolean canShowMore = downloadInfo.isComplete();
                mMoreButton.setVisibility(canShowMore ? View.VISIBLE : View.GONE);
                mMoreButton.setClickable(true);
                setLongClickable(downloadInfo.isComplete());

                ThumbnailProvider thumbnailProvider = DownloadManager.instance().provideThumbnailProvider();
                thumbnailProvider.cancelRetrieval(this);

                mThumbnailBitmap = null;
                thumbnailProvider.getThumbnail(this);
                if (mThumbnailBitmap == null) updateView();
                break;
            }
            case DownloadStatus.FAILED:
            case DownloadStatus.RETIRED: {
                break;
            }

        }

    }

    @Nullable
    @Override
    public String getContentId() {
        return getItem() == null ? "" : Long.toString(Utils.crc64Long(getItem().filePath));
    }

    @Nullable
    @Override
    public String getFilePath() {
        return getItem() == null ? null : getItem().filePath;
    }

    @Override
    public int getIconSize() {
        return mIconSize;
    }

    @Override
    public Item[] getItems() {
        return new Item[]{new Item(getContext(), R.string.share, true),
                new Item(getContext(), R.string.delete, true)};
    }


    @Override
    protected void onClick() {
        DownloadManager.instance().openContent(getItem());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressView = findViewById(R.id.download_progress_view);

        mLayoutContainer = findViewById(R.id.layout_container);
        mLayoutCompleted = findViewById(R.id.completed_layout);
        mLayoutInProgress = findViewById(R.id.progress_layout);

        mFilenameCompletedView = findViewById(R.id.filename_completed_view);
        mDescriptionCompletedView = findViewById(R.id.description_view);
        mMoreButton = findViewById(R.id.more);

        mFilenameInProgressView = findViewById(R.id.filename_progress_view);
        mDownloadStatusView = findViewById(R.id.status_view);
        mDownloadPercentageView = findViewById(R.id.percentage_view);

        mPauseResumeButton = findViewById(R.id.pause_button);
        mCancelButton = findViewById(R.id.cancel_button);

        mMoreButton.setDelegate(this);
        mPauseResumeButton.setOnClickListener(view -> {
            if (getItem().isPaused()) {
                DownloadManager.instance().resume(getItem());
            } else if (!getItem().isComplete()) {
                DownloadManager.instance().pause(getItem());
            }
        });
        mCancelButton.setOnClickListener(view -> {
            DownloadManager.instance().cancel(getItem());
        });
    }

    @Override
    public void onItemSelected(Item item) {
        switch (item.getTextId()) {
            case R.string.delete:
                FileLogger.log("TAG/DownloadItemView", "onItemSelected: delete");
                DownloadManager.instance().delete(getItem());
                break;
        }
    }

    @Override
    public void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap thumbnail) {
        mThumbnailBitmap = thumbnail;
        ThreadUtils.postOnUiThread(this::updateView);
    }

    @Override
    protected void updateView() {
        if (isChecked()) {
            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_selected));
            mIconView.setImageDrawable(mCheckDrawable);
            ApiCompatibilityUtils.setImageTintList(mIconView, mCheckedIconForegroundColorList);
            mCheckDrawable.start();
        } else if (mThumbnailBitmap != null) {
            assert !mThumbnailBitmap.isRecycled();
            mIconView.setBackground(null);


            mIconView.setImageDrawable(ViewUtils.createRoundedBitmapDrawable(
                    Bitmap.createScaledBitmap(mThumbnailBitmap, mIconSize, mIconSize, false),
                    getResources().getDimensionPixelSize(
                            R.dimen.list_item_start_icon_corner_radius)));
            ApiCompatibilityUtils.setImageTintList(mIconView, null);
        } else {


            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_default));
            mIconView.setImageResource(R.drawable.ic_drive_document_24dp);
            ApiCompatibilityUtils.setImageTintList(mIconView, mIconForegroundColorList);
        }
    }
}
