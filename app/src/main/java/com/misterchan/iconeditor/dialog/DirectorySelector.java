package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.InputFilter;
import android.widget.Spinner;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectorySelector {
    private static final Pattern PATTERN_TREE = Pattern.compile("^content://com\\.android\\.externalstorage\\.documents/tree/primary%3A(?<path>.*)$");

    public interface OnApplyFileNameCallback {
        void onApply(Project project);
    }

    public static final class FileNameHelper {
        private static final Pattern PATTERN = Pattern.compile("[\"*/:<>?\\\\|]");

        public static final InputFilter[] FILTERS = new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    final Matcher matcher = PATTERN.matcher(source.toString());
                    if (matcher.find()) {
                        return "";
                    }
                    return null;
                }
        };
    }

    private Context context;
    private OnApplyFileNameCallback callback;
    private String tree = "";
    private Project src, dst;

    private final DialogInterface.OnClickListener onApplyFileNameListener = (dialog, which) -> {
        final TextInputEditText tietFileName = ((AlertDialog) dialog).findViewById(R.id.tiet_file_name);
        final Spinner sFileType = ((AlertDialog) dialog).findViewById(R.id.s_file_type);
        final String fileType = sFileType.getSelectedItem().toString();
        final String fileName = tietFileName.getText().toString() + fileType;
        if (fileName.length() <= 0) {
            return;
        }
        dst.filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + tree + File.separator + fileName;
        dst.fileType = switch (fileType) {
            case ".png" -> Project.FileType.PNG;
            case ".jpg" -> Project.FileType.JPEG;
            case ".gif" -> Project.FileType.GIF;
            case ".webp" -> Project.FileType.WEBP;
            default -> src.fileType;
        };
        dst.compressFormat = switch (fileType) {
            case ".png" -> Bitmap.CompressFormat.PNG;
            case ".jpg" -> Bitmap.CompressFormat.JPEG;
            case ".webp" -> src.compressFormat == Bitmap.CompressFormat.WEBP_LOSSY
                    ? Bitmap.CompressFormat.WEBP_LOSSY : Bitmap.CompressFormat.WEBP_LOSSLESS;
            default -> src.compressFormat;
        };
        dst.setTitle(fileName);
        if (callback != null) {
            callback.onApply(dst);
        }
    };

    private final ActivityResultCallback<Uri> onDocTreeOpenedCallback = result -> {
        if (result == null) {
            return;
        }

        final Matcher matcher = PATTERN_TREE.matcher(result.toString());
        if (!matcher.find()) {
            return;
        }
        tree = matcher.group("path").replace("%2F", "/");

        final AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, onApplyFileNameListener)
                .setTitle(R.string.file_name)
                .setView(R.layout.file_name)
                .show();

        final Project.FileType fileType = src.fileType;
        final Spinner sFileType = dialog.findViewById(R.id.s_file_type);
        final TextInputEditText tietFileName = dialog.findViewById(R.id.tiet_file_name);

        sFileType.setSelection(fileType == null ? 0 : switch (fileType) {
            case PNG -> 0;
            case JPEG -> 1;
            case GIF -> 2;
            case WEBP -> 3;
        });
        tietFileName.setFilters(FileNameHelper.FILTERS);
        tietFileName.setText(src.getName());
    };

    private final ActivityResultLauncher<Uri> openDocTree;

    public DirectorySelector(ComponentActivity activity) {
        context = activity;
        openDocTree = activity.registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), onDocTreeOpenedCallback);
    }

    public void open(Project src, Project dst, OnApplyFileNameCallback callback) {
        this.callback = callback;
        this.src = src;
        this.dst = dst;
        openDocTree.launch(null);
    }
}
