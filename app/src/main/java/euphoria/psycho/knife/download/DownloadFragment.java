package euphoria.psycho.knife.download;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.os.Environment;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.common.ContentUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.NetUtils;
import euphoria.psycho.common.StringUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.base.BaseFragment;
import euphoria.psycho.common.widget.selection.SelectableListLayout;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.common.widget.selection.SelectionDelegate.SelectionObserver;
import euphoria.psycho.knife.R;

public class DownloadFragment extends BaseFragment implements OnMenuItemClickListener, SelectionObserver<DownloadInfo> {

    RecyclerView mRecyclerView;
    SelectableListLayout mContainer;
    DownloadAdapter mAdapter;
    DownloadToolbar mToolbar;
    SelectionDelegate<DownloadInfo> mSelectionDelegate;

    private DownloadManager mDownloadManager;
    private File mDirectory;
    private TextView mEmptyView;
    ClipboardManager mClipboardManager;
    OnPrimaryClipChangedListener mPrimaryClipChangedListener = () -> {
        insertTaskFromClipboard();
    };

    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    private void initializeDirectory() {
        mDirectory = new File(Environment.getExternalStorageDirectory(), "Videos");
        if (!mDirectory.isDirectory()) {
            mDirectory.mkdirs();
        }
    }

    private void insertTaskFromClipboard() {
        String url = ContentUtils.getTextFormClipboard(mClipboardManager);
        if (url == null) return;
        if (!NetUtils.isURL(url)) return;
        String fileName = StringUtils.substringAfterLast(url, "/");
        if (fileName.indexOf('?') != -1)
            fileName = StringUtils.substringBefore(fileName, "?");

        File targetFile = new File(mDirectory, fileName);
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.filePath = targetFile.getAbsolutePath();
        downloadInfo.fileName = fileName;
        downloadInfo.status = DownloadStatus.PAUSED;
        downloadInfo.url = url;
        downloadInfo._id = DownloadDatabase.instance().insert(downloadInfo);
        if (downloadInfo._id > 0) {

            Toast.makeText(getContext(), getString(R.string.message_success_insert_download_task, downloadInfo.fileName), Toast.LENGTH_LONG).show();
            updateRecyclerView();


        }
    }

    private void listenClipboard() {
        if (mClipboardManager == null) {
            mClipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        mClipboardManager.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
        }
        super.onDestroy();

    }

    private void updateRecyclerView() {

        ThreadUtils.postOnBackgroundThread(() -> {
            List<DownloadInfo> downloadInfos = DownloadDatabase.instance().queryPendingTask();

            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDatas(downloadInfos);
            });
        });
    }

    @Override
    protected void bindViews(View view) {

        mContainer = view.findViewById(R.id.container);


    }

    @Override
    protected void initViews() {

        Log.e("TAG/DownloadFragment", "initViews: ");

        initializeDirectory();
        mSelectionDelegate = new SelectionDelegate<>();
        mSelectionDelegate.addObserver(this);
        mAdapter = new DownloadAdapter(mSelectionDelegate, this);

        mRecyclerView = mContainer.initializeRecyclerView(mAdapter);


        mToolbar = (DownloadToolbar) mContainer.initializeToolbar(
                R.layout.download_toolbar,
                mSelectionDelegate,
                R.string.download_title,
                null,
                R.id.normal_menu_group,
                R.id.selection_mode_menu_group,
                this, true, false
        );
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        DownloadManager.instance().setActivity(activity);
        mEmptyView = mContainer.initializeEmptyView(
                VectorDrawableCompat.create(
                        activity.getResources(), R.drawable.downloads_big, activity.getTheme()),
                R.string.download_manager_ui_empty, R.string.download_manager_ui_empty);
        listenClipboard();
        DownloadManager.instance().addObserver(new DownloadObserverImpl(mAdapter));
        updateRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        Log.e("TAG/", "onCreateOptionsMenu: ");

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onSelectionStateChange(List<DownloadInfo> selectedItems) {

    }

    @Override
    protected int provideLayoutId() {
        return R.layout.fragment_download;
    }

    @Override
    protected int provideMenuId() {

        Log.e("TAG/", "provideMenuId: ");

        return 0;
    }
}
