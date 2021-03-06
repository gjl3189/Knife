package euphoria.psycho.knife;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.C;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.helpers.ContextHelper;
import euphoria.psycho.knife.helpers.IconHelper;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailRequest;
import euphoria.psycho.share.util.ThreadUtils;
import euphoria.psycho.share.util.Utils;

public class DocumentView extends SelectableItemView<DocumentInfo> implements ListMenuButton.Delegate, ThumbnailRequest {

    private final ColorStateList mCheckedIconForegroundColorList;
    private final int mIconBackgroundResId;
    DocumentActionDelegate mDelegate;
    private ListMenuButton mMore;
    private int mIconSize;
    private Bitmap mThumbnailBitmap;

    private Drawable mIconDrawable;

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.list_item_start_icon_width);

        mIconBackgroundResId = R.drawable.list_item_icon_modern_bg;
        mCheckedIconForegroundColorList = DocumentUtils.getIconForegroundColorList(context);

    }

    @Nullable
    @Override
    public String getFilePath() {
        return getItem() == null ? "" : getItem().getPath();
    }

    @Nullable
    @Override
    public String getContentId() {
        return getItem() == null ? "" : Long.toString(Utils.crc64Long(getItem().getPath()));
    }

    @Override
    public void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap thumbnail) {
        mThumbnailBitmap = thumbnail;
        ThreadUtils.postOnUiThread(this::updateView);
    }

    @Override
    public int getIconSize() {
        return mIconSize;
    }

    public void initializeActionDelegate(DocumentActionDelegate delegate, SelectionDelegate selectionDelegate) {
        mDelegate = delegate;
        setSelectionDelegate(selectionDelegate);
    }


    //         return getItem() == null ? "" : Long.toString(Utils.crc64Long(getItem().getPath()));


    @Override
    public ListMenuButton.Item[] getItems() {
        return ContextHelper.generateListMenu(getContext(), getItem());
    }


    @Override
    protected void onClick() {
        mDelegate.onClicked(getItem());

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMore = findViewById(R.id.more);
        mMore.setDelegate(this);

    }

    @Override
    public void onItemSelected(ListMenuButton.Item item) {
        switch (item.getTextId()) {
            case R.string.add_to_archive:
                mDelegate.addToArchive(getItem());
                break;
            case R.string.share:
                mDelegate.getListMenuDelegate().shareDocumentInfo(getItem());
                break;
            case R.string.delete:
                mDelegate.delete(getItem());
                break;
            case R.string.trim_video:
                mDelegate.trimVideo(getItem());
                break;
            case R.string.properties:
                mDelegate.getProperties(getItem());
                break;
            case R.string.rename:
                mDelegate.rename(getItem());
                break;
            case R.string.extract:
                mDelegate.unzip(getItem());
                break;
            case R.string.add_bookmark:
                mDelegate.getListMenuDelegate().addToBookmark(getItem());
                break;

            case R.string.copy_file_name:
                mDelegate.copyFileName(getItem());
                break;

            case R.string.copy_content:
                mDelegate.copyContent(getItem());
                break;
            case R.string.format_file_name:
                mDelegate.formatFileName(getItem());
                break;

        }
    }

//
//    public void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap thumbnail) {
//        if (TextUtils.equals(getContentId(), contentId) && thumbnail != null
//                && thumbnail.getWidth() > 0 && thumbnail.getHeight() > 0) {
//            assert !thumbnail.isRecycled();
//            mThumbnailBitmap = thumbnail;
//            updateView();
//        }
//    }

    @Override
    public void setItem(DocumentInfo documentInfo) {
        if (getItem() == documentInfo) return;
        super.setItem(documentInfo);
        mDelegate.getThumbnailProvider().cancelRetrieval(this);
        mThumbnailBitmap = null;

        mTitleView.setText(documentInfo.getFileName());
        mIconDrawable = IconHelper.getIcon(documentInfo.getType());

        updateView();
        switch (documentInfo.getType()) {
            case C.TYPE_DIRECTORY:
                mDescriptionView.setText(getContext().getString(R.string.directory_description, documentInfo.getSize()));
                break;
            case C.TYPE_APK:
            case C.TYPE_IMAGE:
            case C.TYPE_VIDEO:
                mDelegate.getThumbnailProvider().getThumbnail(this);
                break;

        }
        if (documentInfo.getType() != C.TYPE_DIRECTORY) {
            mDescriptionView.setText(Files.formatFileSize(documentInfo.getSize()));
        }
    }

    @Override
    public void setSelectionDelegate(SelectionDelegate<DocumentInfo> delegate) {
        super.setSelectionDelegate(delegate);
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

            mIconView.setImageDrawable(new BitmapDrawable(mThumbnailBitmap));
//            mIconView.setImageDrawable(ViewUtils.createRoundedBitmapDrawable(
//                    Bitmap.createScaledBitmap(mThumbnailBitmap, mIconSize, mIconSize, false),
//                    getResources().getDimensionPixelSize(
//                            R.dimen.list_item_start_icon_corner_radius)));
            ApiCompatibilityUtils.setImageTintList(mIconView, null);
        } else {


            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_default));
            mIconView.setImageDrawable(mIconDrawable);
            ApiCompatibilityUtils.setImageTintList(mIconView, null);
        }
    }
}
