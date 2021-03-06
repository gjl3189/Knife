package euphoria.psycho.common;


/**
 * A simple single-argument callback to handle the result of a computation.
 *
 * @param <T> The type of the computation's result.
 */
public interface Callback<T> {
    /**
     * Invoked with the result of a computation.
     */
    void onResult(T result);

    /**
     * JNI Generator does not know how to target static methods on interfaces
     * (which is new in Java 8, and requires desugaring).
     */
    abstract class Helper {
        @SuppressWarnings("unchecked")
        static void onObjectResultFromNative(Callback callback, Object result) {
            callback.onResult(result);
        }

        @SuppressWarnings("unchecked")
        static void onBooleanResultFromNative(Callback callback, boolean result) {
            callback.onResult(Boolean.valueOf(result));
        }

        @SuppressWarnings("unchecked")
        static void onIntResultFromNative(Callback callback, int result) {
            callback.onResult(Integer.valueOf(result));
        }

        static void runRunnable(Runnable runnable) {
            runnable.run();
        }
    }
}
