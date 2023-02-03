package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

class CellGridManager {

    public interface OnUpdateListener {
        void onUpdate();
    }

    private final AlertDialog.Builder builder;
    private final CellGrid cellGrid;
    private CheckBox cbEnabled;
    private final OnUpdateListener onUpdateListener;
    private TextInputEditText tietOffsetX, tietOffsetY;
    private TextInputEditText tietSizeX, tietSizeY;
    private TextInputEditText tietSpacingX, tietSpacingY;

    public CellGridManager(Context context, CellGrid cellGrid, OnUpdateListener onUpdateListener) {
        this.cellGrid = cellGrid;
        this.onUpdateListener = onUpdateListener;

        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> update())
                .setTitle(R.string.cell_grid)
                .setView(R.layout.cell_grid);
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

    private void update() {
        try {
            int sizeX = Integer.parseInt(tietSizeX.getText().toString()),
                    sizeY = Integer.parseInt(tietSizeY.getText().toString()),
                    spacingX = Integer.parseInt(tietSpacingX.getText().toString()),
                    spacingY = Integer.parseInt(tietSpacingY.getText().toString()),
                    offsetX = Integer.parseInt(tietOffsetX.getText().toString()),
                    offsetY = Integer.parseInt(tietOffsetY.getText().toString());

            cellGrid.enabled = cbEnabled.isChecked();
            cellGrid.sizeX = sizeX;
            cellGrid.sizeY = sizeY;
            cellGrid.spacingX = spacingX;
            cellGrid.spacingY = spacingY;
            cellGrid.offsetX = offsetX;
            cellGrid.offsetY = offsetY;
        } catch (NumberFormatException e) {
        }

        onUpdateListener.onUpdate();
    }
}
