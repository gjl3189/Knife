package euphoria.psycho.common;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

// https://android.googlesource.com/platform/packages/apps/UnifiedEmail/+/kitkat-mr1-release/src/org/apache/commons/io

/**
 * Helper methods for dealing with Files.
 */
public class FileUtils {
public static final char EXTENSION_SEPARATOR = '.';
        private static final String TAG = "FileUtils";
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Delete the given files or directories by calling {@link #recursivelyDeleteFile(File)}.
     *
     * @param files The files to delete.
     */
    public static void batchDeleteFiles(List<File> files) {
        ThreadUtils.assertOnBackgroundThread();

        for (File file : files) {
            if (file.exists()) recursivelyDeleteFile(file);
        }
    }

    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {

        }
    }

    /**
     * Atomically copies the data from an input stream into an output file.
     *
     * @param is      Input file stream to read data from.
     * @param outFile Output file path.
     * @param buffer  Caller-provided buffer. Provided to avoid allocating the same
     *                buffer on each call when copying several files in sequence.
     * @throws IOException in case of I/O error.
     */
    public static void copyFileStreamAtomicWithBuffer(InputStream is, File outFile, byte[] buffer)
            throws IOException {
        File tmpOutputFile = new File(outFile.getPath() + ".tmp");
        try (OutputStream os = new FileOutputStream(tmpOutputFile)) {
            Log.i(TAG, "Writing to %s", outFile);

            int count = 0;
            while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, count);
            }
        }
        if (!tmpOutputFile.renameTo(outFile)) {
            throw new IOException();
        }
    }

    /**
     * Extracts an asset from the app's APK to a file.
     *
     * @param context
     * @param assetName Name of the asset to extract.
     * @param dest      File to extract the asset to.
     * @return true on success.
     */
    public static boolean extractAsset(Context context, String assetName, File dest) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);
            outputStream = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[8192];
            int c;
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    public static String formatFileSize(long number) {
        float result = number;
        String suffix = "";
        if (result > 900) {
            suffix = " KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.1f", result);
        } else if (result < 100) {
            value = String.format("%.0f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return value + suffix;
    }

    public static String getDirectoryName(String file) {
        int index = file.lastIndexOf('/');
        if (index == -1) return "";
        return file.substring(0, index);
    }

public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Returns a URI that points at the file.
     *
     * @param file File to get a URI for.
     * @return URI that points at that file, either as a content:// URI or a file:// URI.
     */
    public static Uri getUriForFile(File file) {
        // TODO(crbug/709584): Uncomment this when http://crbug.com/709584 has been fixed.
        // assert !ThreadUtils.runningOnUiThread();
        Uri uri = null;

        try {
            // Try to obtain a content:// URI, which is preferred to a file:// URI so that
            // receiving apps don't attempt to determine the file's mime type (which often fails).
            uri = ContentUriUtils.getContentUriFromFile(file);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Could not create content uri: " + e);
        }

        if (uri == null) uri = Uri.fromFile(file);

        return uri;
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Delete the given File and (if it's a directory) everything within it.
     */
    public static void recursivelyDeleteFile(File currentFile) {
        ThreadUtils.assertOnBackgroundThread();
        if (currentFile.isDirectory()) {
            File[] files = currentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    recursivelyDeleteFile(file);
                }
            }
        }

        if (!currentFile.delete()) Log.e(TAG, "Failed to delete: " + currentFile);
    }

    public static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }

        return size;
    }
}
