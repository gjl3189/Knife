package euphoria.psycho.common;

import android.content.ClipData;
import android.content.ClipboardManager;

public class ContentUtils {

    public static String getTextFormClipboard(ClipboardManager manager) {
        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() < 1) return null;

        CharSequence charSequence = clipData.getItemAt(0).getText();
        if (charSequence == null) return null;
        return charSequence.toString();
    }
}
