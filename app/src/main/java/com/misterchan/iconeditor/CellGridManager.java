package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class CellGridManager {

    public interface OnUpdateListener {
        void onUpdate();
    }

    private AlertDialog.Builder builder;
    private CellGrid cellGrid;
    private CheckBox cbEnabled;
    private EditText etOffsetX, etOffsetY;
    private EditText etSizeX, etSizeY;
    private EditText etSpacingX, etSpacingY;
    private OnUpdateListener onUpdateListener;

    private final DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
        try {
            int sizeX = Integer.parseInt(etSizeX.getText().toString()),
                    sizeY = Integer.parseInt(etSizeY.getText().toString()),
                    spacingX = Integer.parseInt(etSpacingX.getText().toString()),
                    spacingY = Integer.parseInt(etSpacingY.getText().toString()),
                    offsetX = Integer.parseInt(etOffsetX.getText().toString()),
                    offsetY = Integer.parseInt(etOffsetY.getText().toString());

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
    };

    public static CellGridManager make(Context context, CellGrid cellGrid, OnUpdateListener onUpdateListener) {

        CellGridManager manager = new CellGridManager();

        manager.cellGrid = cellGrid;
        manager.onUpdateListener = onUpdateListener;

        manager.builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, manager.onClickListener)
                .setTitle(R.string.cell_grid)
                .setView(R.layout.cell_grid);

        return manager;
    }

    public void show() {

        final AlertDialog dialog = builder.show();

        cbEnabled = dialog.findViewById(R.id.cb_enabled);
        etSizeX = dialog.findViewById(R.id.et_size_x);
        etSizeY = dialog.findViewById(R.id.et_size_y);
        etSpacingX = dialog.findViewById(R.id.et_spacing_x);
        etSpacingY = dialog.findViewById(R.id.et_spacing_y);
        etOffsetX = dialog.findViewById(R.id.et_offset_x);
        etOffsetY = dialog.findViewById(R.id.et_offset_y);

        cbEnabled.setChecked(cellGrid.enabled);
        etSizeX.setText(String.valueOf(cellGrid.sizeX));
        etSizeY.setText(String.valueOf(cellGrid.sizeY));
        etSpacingX.setText(String.valueOf(cellGrid.spacingX));
        etSpacingY.setText(String.valueOf(cellGrid.spacingY));
        etOffsetX.setText(String.valueOf(cellGrid.offsetX));
        etOffsetY.setText(String.valueOf(cellGrid.offsetY));
    }
}
