package euphoria.common;

import android.widget.Toast;

import euphoria.psycho.knife.Contexts;

public class Logs {

    public static void t(String message) {
        if (Contexts.getContext() != null)
            Toast.makeText(Contexts.getContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void t(Exception e) {
        t(String.format("Message: %s\nCause: %s\n"
                , e.getMessage()
                , e.getCause()));

    }
}
