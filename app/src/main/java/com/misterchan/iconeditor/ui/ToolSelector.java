package com.misterchan.iconeditor.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.databinding.ToolsBinding;

class ToolSelector {
    private ToolSelector() {
    }

    @SuppressLint("NonConstantResourceId")
    public static void show(MainActivity ma, TextView buttonView) {
        @IdRes final int menuGroupId;
        final TextView[] toolButtons;
        final ToolsBinding tools = ma.getBinding().tools;
        switch (buttonView.getId()) {
            case R.id.b_eyedropper, R.id.b_ruler -> {
                menuGroupId = R.id.g_eyedropper;
                toolButtons = new TextView[]{tools.bEyedropper, tools.bRuler};
            }
            case R.id.b_pencil, R.id.b_brush, R.id.b_magic_paint -> {
                menuGroupId = R.id.g_pencil;
                toolButtons = new TextView[]{tools.bPencil, tools.bBrush, tools.bMagicPaint};
            }
            case R.id.b_eraser, R.id.b_magic_eraser -> {
                menuGroupId = R.id.g_eraser;
                toolButtons = new TextView[]{tools.bEraser, tools.bMagicEraser};
            }
            case R.id.b_line, R.id.b_rect, R.id.b_oval, R.id.b_circle, R.id.b_gradient_line -> {
                menuGroupId = R.id.g_shape;
                toolButtons = new TextView[]{tools.bLine, tools.bRect, tools.bOval, tools.bCircle, tools.bGradientLine};
            }
            case R.id.b_bucket_fill, R.id.b_gradient -> {
                menuGroupId = R.id.g_fill;
                toolButtons = new TextView[]{tools.bBucketFill, tools.bGradient};
            }
            default -> {
                return;
            }
        }

        final PopupMenu popupMenu = new PopupMenu(ma, buttonView);
        final Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.tools, menu);
        menu.setGroupVisible(menuGroupId, true);

        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            if (item.getGroupId() == menuGroupId && item.getTitle().toString().equals(buttonView.getText().toString())) {
                item.setChecked(true);
                break;
            }
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            @IdRes int toolButtonId = 0;
            for (final TextView tb : toolButtons) {
                if (tb.getText().toString().equals(item.getTitle().toString())) {
                    tb.setVisibility(View.VISIBLE);
                    toolButtonId = tb.getId();
                } else {
                    tb.setVisibility(View.GONE);
                }
            }
            if (tools.btgTools.getCheckedButtonId() != toolButtonId) {
                tools.btgTools.check(toolButtonId);
            }
            return true;
        });

        popupMenu.show();
    }
}
