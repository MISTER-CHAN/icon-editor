package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap gridBitmap;
    private Bitmap viewBitmap;
    private final BitmapHistory history = new BitmapHistory();
    private boolean hasNotLoaded = true;
    private Canvas canvas;
    private Canvas gridCanvas;
    private Canvas viewCanvas;
    private final CellGrid cellGrid = new CellGrid();
    private CheckBox cbCellGridEnabled;
    private Bitmap.CompressFormat compressFormat = null;
    private double prevDiagonal = 0.0;
    private EditText etCellGridSizeX, etCellGridSizeY;
    private EditText etRed, etGreen, etBlue, etAlpha;
    private float pivotX = 0.0f, pivotY = 0.0f;
    private float prevX = 0.0f, prevY = 0.0f;
    private float scale = 20.0f;
    private FrameLayout flBackground;
    private FrameLayout flImageView;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private RadioButton rbBackgroundColor;
    private RadioButton rbForegroundColor;
    private RadioButton rbColor;
    private SeekBar sbRed, sbGreen, sbBlue, sbAlpha;
    private String path = null;

    private final Paint backgroundPaint = new Paint() {

        {
            setAntiAlias(false);
            setColor(Color.WHITE);
            setDither(false);
        }
    };

    private final Paint cellGridPaint = new Paint() {

        {
            setColor(Color.RED);
        }
    };

    private final Paint foregroundPaint = new Paint() {

        {
            setAntiAlias(false);
            setColor(Color.BLACK);
            setDither(false);
        }
    };

    private final Paint gridPaint = new Paint() {

        {
            setColor(Color.GRAY);
        }
    };

    private Paint paint = foregroundPaint;

    private final ActivityResultCallback<Uri> imageActivityResultCallback = result -> {
        if (result == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(result)) {
            Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeStream(inputStream));
            bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(bm, 0.0f, 0.0f, paint);

            scale = (float) ((double) imageView.getWidth() / (double) bm.getWidth());

            bm.recycle();

            int width = imageView.getWidth(), height = (int) ((double) bitmap.getHeight() / (double) bitmap.getWidth() * imageView.getWidth());
            viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            viewCanvas = new Canvas(viewBitmap);
            drawBitmapOnView();
            imageView.setImageBitmap(viewBitmap);

            gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            gridCanvas = new Canvas(gridBitmap);
            drawGridOnView();
            ivGrid.setImageBitmap(gridBitmap);

            path = UriToPathUtil.getRealFilePath(this, result);

            switch (getContentResolver().getType(result)) {
                case "image/jpg":
                    compressFormat = Bitmap.CompressFormat.JPEG;
                    break;
                case "image/png":
                    compressFormat = Bitmap.CompressFormat.PNG;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private final ActivityResultLauncher<String> imageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imageActivityResultCallback);

    private final CompoundButton.OnCheckedChangeListener onBackgroundColorRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            paint = backgroundPaint;
            rbColor = rbBackgroundColor;
            showPaintColorOnSeekBars();
        }
    };

    private final CompoundButton.OnCheckedChangeListener onForegroundColorRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            paint = foregroundPaint;
            rbColor = rbForegroundColor;
            showPaintColorOnSeekBars();
        }
    };

    private final DialogInterface.OnClickListener onCellGridDialogPosButtonClickListener = (dialog, which) -> {
        try {
            cellGrid.enabled = cbCellGridEnabled.isChecked();
            cellGrid.sizeX = Integer.parseInt(etCellGridSizeX.getText().toString());
            cellGrid.sizeY = Integer.parseInt(etCellGridSizeY.getText().toString());
        } catch (NumberFormatException e) {}
        drawGridOnView();
        ivGrid.setImageBitmap(gridBitmap);
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onBackgroundTouchWithScalerListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1:
                float x = event.getX(), y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prevX = x;
                        prevY = y;
                        drawBitmapOnView();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        setTranslation(flImageView, flImageView.getTranslationX() + x - prevX, flImageView.getTranslationY() + y - prevY);
                        prevX = x;
                        prevY = y;
                        break;
                }
                break;

            case 2:
                float x0 = event.getX(0), y0 = event.getY(0),
                        x1 = event.getX(1), y1 = event.getY(1);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        double diagonalRatio = diagonal / prevDiagonal;
                        float scale = (float) (this.scale * diagonalRatio);
                        int scaledWidth = (int) (bitmap.getWidth() * scale), scaledHeight = (int) (bitmap.getHeight() * scale);
                        if (scaledWidth > 4096 || scaledHeight > 4096) {
                            break;
                        }
                        this.scale = scale;
                        setSize(flImageView, scaledWidth, scaledHeight);
                        float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        setTranslation(flImageView, (int) (flImageView.getTranslationX() - pivotX + this.pivotX), flImageView.getTranslationY() - pivotY + this.pivotY);
                        setPivot(pivotX, pivotY);
                        prevDiagonal = diagonal;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        setPivot((x0 + x1) / 2.0f - flImageView.getTranslationX(), (y0 + y1) / 2.0f - flImageView.getTranslationY());
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        int width = flImageView.getWidth(), height = flImageView.getHeight();
                        viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        viewCanvas = new Canvas(viewBitmap);
                        gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                        gridCanvas = new Canvas(gridBitmap);
                        drawGridOnView();
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
                        break;
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = x;
                prevY = y;
                canvas.drawPoint(toOriginal(x), toOriginal(y), paint);
                break;
            case MotionEvent.ACTION_MOVE:
                canvas.drawLine(toOriginal(prevX), toOriginal(prevY), toOriginal(x), toOriginal(y), paint);
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                break;
        }
        drawVisiblePartOfBitmapOnView();
        imageView.setImageBitmap(viewBitmap);
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithScalerListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX(), y = event.getY();
                break;
        }
        return false;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPenRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            flBackground.setOnTouchListener(null);
            flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onScalerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            flImageView.setOnTouchListener(null);
            flBackground.setOnTouchListener(onBackgroundTouchWithScalerListener);
        }
    };

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void drawBitmapOnView() {
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        Paint paint = new Paint();
        clearCanvas(viewCanvas);
        float top = 0.0f, bottom = scale;
        for (int y = 0; y < height; ++y, top += scale, bottom += scale) {
            float left = 0.0f;
            for (int x = 0; x < width; ++x) {
                paint.setColor(bitmap.getPixel(x, y));
                viewCanvas.drawRect(left, top, left += scale, bottom, paint);
            }
        }
        imageView.setImageBitmap(viewBitmap);
    }

    private void drawVisiblePartOfBitmapOnView() {
        Paint paint = new Paint();
        clearCanvas(viewCanvas);
        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
        float translationX = flImageView.getTranslationX(), translationY = flImageView.getTranslationY();
        float screenWidth = flBackground.getWidth(), screenHeight = flBackground.getHeight();
        int viewWidth = flImageView.getWidth(), viewHeight = flImageView.getHeight();
        int startX = (int) (Math.max(0.0f, -translationX) / viewWidth * bitmapWidth),
                startY = (int) (Math.max(0.0f, -translationY) / viewHeight * bitmapHeight);
        int endX = (int) (Math.min(viewWidth, screenWidth - translationX) / viewWidth * bitmapWidth),
                endY = (int) (Math.min(viewHeight, screenHeight - translationY) / viewHeight * bitmapHeight);
        float top = toScaled(startY), bottom = top + scale;
        for (int y = startY; y < endY; ++y, top += scale, bottom += scale) {
            float left = toScaled(startX);
            for (int x = startX; x < endX; ++x) {
                paint.setColor(bitmap.getPixel(x, y));
                viewCanvas.drawRect(left, top, left += scale, bottom, paint);
            }
        }
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);
        int viewWidth = ivGrid.getWidth(), viewHeight = ivGrid.getHeight();
        for (float x = 0.0f; x < viewWidth; x += scale) {
            gridCanvas.drawLine(x, 0.0f, x, viewHeight, gridPaint);
        }
        for (float y = 0.0f; y < viewHeight; y += scale) {
            gridCanvas.drawLine(0.0f, y, viewWidth, y, gridPaint);
        }
        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 0) {
                float scaledSizeX = toScaled(cellGrid.sizeX);
                for (float x = toScaled(cellGrid.offsetX); x < viewWidth; x += scaledSizeX) {
                    gridCanvas.drawLine(x, 0.0f, x, viewHeight, cellGridPaint);
                }
            }
            if (cellGrid.sizeY > 0) {
                float scaledSizeY = toScaled(cellGrid.sizeY);
                for (float y = toScaled(cellGrid.offsetY); y < viewWidth; y += scaledSizeY) {
                    gridCanvas.drawLine(0.0f, y, viewWidth, y, cellGridPaint);
                }
            }
        }
    }

    private void drawVisiblePartOfGridOnView() {

//        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
//        float startX = (float) (Math.floor(Math.min(0.0f, -flImageView.getTranslationX()) / bitmapWidth) * bitmapWidth),
//                startY = (float) (Math.floor(Math.min(0.0f, -flImageView.getTranslationY()) / bitmapHeight) * bitmapHeight);
//        float endX = (float) ()
//        for (float y = ) {
//            for (float x = )
//        }
    }

    private void load() {
        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        history.offer(bitmap);

        int width = imageView.getWidth();
        viewBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        setPivot(imageView, 0.0f, 0.0f);
        imageView.setImageBitmap(viewBitmap);

        gridBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        drawGridOnView();
        setPivot(ivGrid, 0.0f, 0.0f);
        ivGrid.setImageBitmap(gridBitmap);

        setPivot(ivChessboard, 0.0f, 0.0f);
    }

    private void onChannelChanged(String hex, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(hex, 16));
            int color = Color.argb(
                    sbAlpha.getProgress(),
                    sbRed.getProgress(),
                    sbGreen.getProgress(),
                    sbBlue.getProgress());
            paint.setColor(color);
            rbColor.setTextColor(color);

        } catch (Exception e) {
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAlpha = findViewById(R.id.et_alpha);
        etBlue = findViewById(R.id.et_blue);
        etGreen = findViewById(R.id.et_green);
        etRed = findViewById(R.id.et_red);
        flBackground = findViewById(R.id.fl_background);
        flImageView = findViewById(R.id.fl_iv);
        imageView = findViewById(R.id.iv);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        rbBackgroundColor = findViewById(R.id.rb_background_color);
        rbForegroundColor = findViewById(R.id.rb_foreground_color);
        rbColor = rbForegroundColor;
        sbAlpha = findViewById(R.id.sb_alpha);
        sbBlue = findViewById(R.id.sb_blue);
        sbGreen = findViewById(R.id.sb_green);
        sbRed = findViewById(R.id.sb_red);

        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbAlpha));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbBlue));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbGreen));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbRed));
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        rbBackgroundColor.setOnCheckedChangeListener(onBackgroundColorRadioButtonCheckedChangeListener);
        rbForegroundColor.setOnCheckedChangeListener(onForegroundColorRadioButtonCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rb_pencil)).setOnCheckedChangeListener(onPenRadioButtonCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rb_scaler)).setOnCheckedChangeListener(onScalerRadioButtonCheckedChangeListener);
        sbAlpha.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etAlpha.setText(String.format("%02X", progress)));
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etBlue.setText(String.format("%02X", progress)));
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etGreen.setText(String.format("%02X", progress)));
        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etRed.setText(String.format("%02X", progress)));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {

        canvas = null;
        bitmap.recycle();
        bitmap = null;

        viewCanvas = null;
        viewBitmap.recycle();
        viewBitmap = null;

        gridCanvas = null;
        gridBitmap.recycle();
        gridBitmap = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_cell_grid:
                AlertDialog cellGridDialog = new AlertDialog.Builder(this)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", onCellGridDialogPosButtonClickListener)
                        .setTitle("Cell Grid")
                        .setView(R.layout.cell_grid)
                        .show();
                cbCellGridEnabled = cellGridDialog.findViewById(R.id.cb_cg_enabled);
                etCellGridSizeX = cellGridDialog.findViewById(R.id.et_cg_size_x);
                etCellGridSizeY = cellGridDialog.findViewById(R.id.et_cg_size_y);
                cbCellGridEnabled.setChecked(cellGrid.enabled);
                etCellGridSizeX.setText(String.valueOf(cellGrid.sizeX));
                etCellGridSizeY.setText(String.valueOf(cellGrid.sizeY));
                break;

            case R.id.i_open:
                imageActivityResultLauncher.launch("image/*");
                break;

            case R.id.i_redo:
                if (history.canRedo()) {
                    clearCanvas(canvas);
                    canvas.drawBitmap(history.redo(), 0.0f, 0.0f, paint);
                    drawVisiblePartOfBitmapOnView();
                    imageView.setImageBitmap(viewBitmap);
                }
                break;

            case R.id.i_save:
                save();
                break;

            case R.id.i_undo:
                if (history.canUndo()) {
                    clearCanvas(canvas);
                    canvas.drawBitmap(history.undo(), 0.0f, 0.0f, paint);
                    drawVisiblePartOfBitmapOnView();
                    imageView.setImageBitmap(viewBitmap);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasNotLoaded && hasFocus) {
            hasNotLoaded = false;
            load();
        }
    }

    private void save() {
        if (path == null) {

        } else {
            File file = new File(path);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(compressFormat, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPivot(float x, float y) {
        pivotX = x;
        pivotY = y;
    }

    private void setPivot(View view, float x, float y) {
        view.setPivotX(x);
        view.setPivotY(y);
    }

    private void setSize(View view, int width, int height) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    private void setTranslation(View view, float x, float y) {
        view.setTranslationX(x);
        view.setTranslationY(y);
    }

    private void showPaintColorOnSeekBars() {
        int color = paint.getColor();
        int red = Color.red(color),
                green = Color.green(color),
                blue = Color.blue(color),
                alpha = Color.alpha(color);

        sbRed.setProgress(red);
        etRed.setText(String.format("%02X", red));

        sbGreen.setProgress(green);
        etGreen.setText(String.format("%02X", green));

        sbBlue.setProgress(blue);
        etBlue.setText(String.format("%02X", blue));

        sbAlpha.setProgress(alpha);
        etAlpha.setText(String.format("%02X", alpha));
    }

    private float toOriginal(float scaled) {
        return scaled / scale;
    }

    private float toScaled(float original) {
        return original * scale;
    }
}