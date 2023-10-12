package com.misterchan.iconeditor.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.misterchan.iconeditor.Frame;
import com.misterchan.iconeditor.Layer;
import com.misterchan.iconeditor.Layers;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileUtils {
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

    public static void export(Activity activity, Project project, int quality) {
        final File file = new File(project.filePath);
        final Frame frame = project.getSelectedFrame();
        final Layer layer = frame.getSelectedLayer();
        final Bitmap bitmap = layer.bitmap;
        if (project.compressFormat != null) {
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(project.compressFormat, quality, fos);
                fos.flush();
            } catch (final IOException e) {
                final View contentView = activity.findViewById(android.R.id.content);
                Snackbar.make(contentView, activity.getString(R.string.failed) + '\n' + e.getMessage(), Snackbar.LENGTH_LONG)
                        .setTextMaxLines(Integer.MAX_VALUE)
                        .show();
                return;
            }
        } else if (project.fileType == Project.FileType.GIF) {
            final GifEncoder gifEncoder = new GifEncoder();
            final int width = layer.bitmap.getWidth(), height = layer.bitmap.getHeight();
            try {
                gifEncoder.init(width, height, project.filePath);
            } catch (FileNotFoundException e) {
                return;
            }
            gifEncoder.setDither(project.gifDither);
            gifEncoder.encodeFrame(bitmap, frame.delay);
            gifEncoder.close();
        }
        MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null, null);
    }

    public static void save(Activity activity, Project project, int quality) {
        final File file = new File(project.filePath);
        if (project.compressFormat != null) {
            final Bitmap merged = Layers.mergeLayers(project.getSelectedFrame().layerTree);
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                merged.compress(project.compressFormat, quality, fos);
                fos.flush();
            } catch (final IOException e) {
                final View contentView = activity.findViewById(android.R.id.content);
                Snackbar.make(contentView, activity.getString(R.string.failed) + '\n' + e.getMessage(), Snackbar.LENGTH_LONG)
                        .setTextMaxLines(Integer.MAX_VALUE)
                        .show();
                e.printStackTrace();
                return;
            } finally {
                merged.recycle();
            }
            MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null, null);

        } else if (project.fileType == Project.FileType.GIF) {
            final GifEncoder gifEncoder = new GifEncoder();
            final Bitmap ffblb = project.getFirstFrame().getBackgroundLayer().bitmap;
            final int width = ffblb.getWidth(), height = ffblb.getHeight();
            try {
                gifEncoder.init(width, height, project.filePath);
            } catch (FileNotFoundException e) {
                return;
            }
            gifEncoder.setDither(project.gifDither);
            final int size = project.frames.size();
            final AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.exporting)
                    .setView(R.layout.progress_indicator)
                    .setCancelable(false)
                    .show();
            final LinearProgressIndicator pi = dialog.findViewById(R.id.progress_indicator);
            pi.setMax(size);
            new Thread(() -> {
                final List<String> invalidFrames = new LinkedList<>();
                for (int i = 0; i < project.frames.size(); ++i) {
                    final Frame f = project.frames.get(i);
                    final Bitmap blb = f.getBackgroundLayer().bitmap;
                    if (blb.getWidth() == width && blb.getHeight() == height
                            && blb.getConfig() == Bitmap.Config.ARGB_8888) {
                        final Bitmap merged = Layers.mergeLayers(f.layerTree);
                        gifEncoder.encodeFrame(merged, f.delay);
                        merged.recycle();
                    } else {
                        invalidFrames.add(String.valueOf(i));
                    }
                    final int progress = i + 1;
                    activity.runOnUiThread(() -> pi.setProgressCompat(progress, true));
                }
                gifEncoder.close();
                MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null, null);
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    if (invalidFrames.isEmpty()) {
                        final View contentView = activity.findViewById(android.R.id.content);
                        Snackbar.make(contentView, R.string.done, Snackbar.LENGTH_SHORT).show();
                    } else {
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(R.string.done)
                                .setMessage(activity.getString(R.string.there_are_frames_invalid_which_are,
                                        invalidFrames.size(),
                                        String.join(", ", invalidFrames)))
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });
            }).start();
        }
    }
}
