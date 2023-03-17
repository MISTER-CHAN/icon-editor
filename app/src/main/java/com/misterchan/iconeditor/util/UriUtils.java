package com.misterchan.iconeditor.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class UriUtils {
    private static final String[] PROJECTION = new String[]{MediaStore.MediaColumns.DATA};

    private static final Uri URI_PUBLIC_DOWNLOADS = Uri.parse("content://downloads/public_downloads");

    private static String getMediaDataColumnValue(ContentResolver contentResolver, Uri uri, String selection, String[] selectionArgs) {
        try (final Cursor cursor = contentResolver.query(uri, PROJECTION, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                return cursor.getString(index);
            }
        }
        return null;
    }

    public static String getRealPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        final String scheme = uri.getScheme();
        if (scheme == null || ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri.getPath();
        }
        final ContentResolver contentResolver = context.getContentResolver();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            {
                final String cv = getMediaDataColumnValue(contentResolver, uri, null, null);
                if (cv != null) {
                    return cv;
                }
            }
            final String authority = uri.getAuthority();
            if (DocumentsContract.isDocumentUri(context, uri)) {
                switch (authority) {
                    case "com.android.externalstorage.documents" -> {
                        final String[] docId = DocumentsContract.getDocumentId(uri).split(":");
                        if ("primary".equals(docId[0])) {
                            return Environment.getExternalStorageDirectory() + "/" + docId[1];
                        }
                    }
                    case "com.android.providers.downloads.documents" -> {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final Uri contentUri = ContentUris.withAppendedId(URI_PUBLIC_DOWNLOADS, Long.parseLong(docId));
                        final String cv = getMediaDataColumnValue(contentResolver, contentUri, null, null);
                        if (cv != null) {
                            return cv;
                        }
                    }
                    case "com.android.providers.media.documents" -> {
                        final String[] docId = DocumentsContract.getDocumentId(uri).split(":");
                        final Uri contentUri = switch (docId[0]) {
                            case "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            case "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            case "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            default -> null;
                        };
                        if (contentUri == null) {
                            return null;
                        }
                        return getMediaDataColumnValue(contentResolver, contentUri,
                                MediaStore.Images.Media._ID + "=?", new String[]{docId[1]});
                    }
                }
            } else if ("com.google.android.apps.photos.content".equals(authority)) {
                return uri.getLastPathSegment();
            } else {
                return getMediaDataColumnValue(contentResolver, uri, null, null);
            }
        }
        return null;
    }
}
