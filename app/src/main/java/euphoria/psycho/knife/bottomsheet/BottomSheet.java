package euphoria.psycho.knife.bottomsheet;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.knife.R;

public class BottomSheet {

    private final Context mContext;
    private BottomSheetAdapter mBottomSheetAdapter;
    private View mContainer;
    private BottomSheetDialog mDialog;
    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClicked(Pair<Integer, String> item) {
            mDialog.dismiss();
            if (mOnClickListener != null) mOnClickListener.onClicked(item);
        }
    };
    private OnClickListener mOnClickListener;
    private RecyclerView mRecyclerView;

    public BottomSheet(Context context) {
        mContext = context;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void showDialog() {
        if (mContainer == null) {
            mContainer = LayoutInflater.from(mContext).inflate(R.layout.bottomsheet_menu, null);
            mRecyclerView = mContainer.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));
            mBottomSheetAdapter = new BottomSheetAdapter(mListener, new Pair[]{
                    Pair.create(R.drawable.ic_action_storage, mContext.getString(R.string.menu_internal_storage)),
                    Pair.create(R.drawable.ic_action_sd_card, mContext.getString(R.string.menu_sd_card)),
                    Pair.create(R.drawable.ic_action_file_download, "Download"),
                    Pair.create(R.drawable.ic_action_photo, "Picture"),
                    Pair.create(R.drawable.ic_create_new_folder_blue_24px,
                            mContext.getString(R.string.menu_create_new_directory)),
                    Pair.create(R.drawable.ic_file_download_blue_24px, mContext.getString(R.string.menu_download_tools)),
                    Pair.create(R.drawable.ic_phonelink_blue_24px, mContext.getString(R.string.menu_connect_pc))
            });
            mRecyclerView.setAdapter(mBottomSheetAdapter);

            BottomSheetDialog dialog = new BottomSheetDialog(mContext);
            dialog.setContentView(mContainer);

            mDialog = dialog;

        }

        mDialog.show();


    }




    public interface OnClickListener {
        void onClicked(Pair<Integer, String> item);
    }
}
