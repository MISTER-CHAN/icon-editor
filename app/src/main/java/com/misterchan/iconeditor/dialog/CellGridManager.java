package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.CellGrid;
import com.misterchan.iconeditor.R;

public class CellGridManager {

    public interface OnApplyListener {
        void onApply();
    }

    private final AlertDialog.Builder builder;
    private final CellGrid cellGrid;
    private CheckBox cbEnabled;
    private final OnApplyListener onUpdateListener;
    private TextInputEditText tietOffsetX, tietOffsetY;
    private TextInputEditText tietSizeX, tietSizeY;
    private TextInputEditText tietSpacingX, tietSpacingY;

    public CellGridManager(Context context, CellGrid cellGrid, OnApplyListener onUpdateListener) {
        this.cellGrid = cellGrid;
        this.onUpdateListener = onUpdateListener;

        builder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_grid_on)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, this::onPositiveButtonClick)
                .setTitle(R.string.cell_grid)
                .setView(R.layout.cell_grid);
    }

    private void onPositiveButtonClick(DialogInterface dialog, int which) {
        final int sizeX, sizeY, spacingX, spacingY, offsetX, offsetY;

        try {
            sizeX = Integer.parseInt(tietSizeX.getText().toString());
            sizeY = Integer.parseInt(tietSizeY.getText().toString());
            spacingX = Integer.parseInt(tietSpacingX.getText().toString());
            spacingY = Integer.parseInt(tietSpacingY.getText().toString());
            offsetX = Integer.parseInt(tietOffsetX.getText().toString());
            offsetY = Integer.parseInt(tietOffsetY.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }

        cellGrid.enabled = cbEnabled.isChecked();
        cellGrid.sizeX = sizeX;
        cellGrid.sizeY = sizeY;
        cellGrid.spacingX = spacingX;
        cellGrid.spacingY = spacingY;
        cellGrid.offsetX = offsetX;
        cellGrid.offsetY = offsetY;

        onUpdateListener.onApply();
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        cbEnabled = dialog.findViewById(R.id.cb_enabled);
        tietSizeX = dialog.findViewById(R.id.tiet_size_x);
        tietSizeY = dialog.findViewById(R.id.tiet_size_y);
        tietSpacingX = dialog.findViewById(R.id.tiet_spacing_x);
        tietSpacingY = dialog.findViewById(R.id.tiet_spacing_y);
        tietOffsetX = dialog.findViewById(R.id.tiet_offset_x);
        tietOffsetY = dialog.findViewById(R.id.tiet_offset_y);

        cbEnabled.setChecked(cellGrid.enabled);
        tietSizeX.setText(String.valueOf(cellGrid.sizeX));
        tietSizeY.setText(String.valueOf(cellGrid.sizeY));
        tietSpacingX.setText(String.valueOf(cellGrid.spacingX));
        tietSpacingY.setText(String.valueOf(cellGrid.spacingY));
        tietOffsetX.setText(String.valueOf(cellGrid.offsetX));
        tietOffsetY.setText(String.valueOf(cellGrid.offsetY));
    }
}
