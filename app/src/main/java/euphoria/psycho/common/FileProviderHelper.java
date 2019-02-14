// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package euphoria.psycho.common;

import android.content.Context;
import android.net.Uri;


import java.io.File;

import androidx.core.content.FileProvider;

/**
 * Utilities for translating a file into content URI.
 */
public class FileProviderHelper implements ContentUriUtils.FileProviderUtil {
    // Keep this variable in sync with the value defined in file_paths.xml.
    private static final String API_AUTHORITY_SUFFIX = ".FileProvider";

    @Override
    public Uri getContentUriFromFile(File file) {
        Context appContext = ContextUtils.getApplicationContext();
        return FileProvider.getUriForFile(
                appContext, appContext.getPackageName() + API_AUTHORITY_SUFFIX, file);
    }
}
