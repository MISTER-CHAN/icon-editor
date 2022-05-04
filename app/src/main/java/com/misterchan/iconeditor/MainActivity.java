package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;

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
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap gridBitmap;
    private Bitmap viewBitmap;
    private BitmapHistory history;
    private boolean hasNotLoaded = true;
    private Canvas canvas;
    private Canvas chessboardCanvas;
    private Canvas gridCanvas;
    private Canvas viewCanvas;
    private final CellGrid cellGrid = new CellGrid();
    private CheckBox cbCellGridEnabled;
    private Bitmap.CompressFormat compressFormat = null;
    private double prevDiagonal;
    private EditText etCellGridSizeX, etCellGridSizeY;
    private EditText etNewImageSizeX, etNewImageSizeY;
    private EditText etRed, etGreen, etBlue, etAlpha;
    private float pivotX, pivotY;
    private float prevX, prevY;
    private float scale;
    private float translationX, translationY;
    private FrameLayout flImageView;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private int imageWidth, imageHeight;
    private int viewWidth, viewHeight;
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

    private final Paint eraser = new Paint() {

        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
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

    private final Paint opaquePaint = new Paint();

    private final Paint pointPaint = new Paint() {

        {
            setColor(Color.RED);
            setStrokeWidth(4.0f);
            setTextSize(32.0f);
        }
    };

    private Paint paint = foregroundPaint;

    private final ActivityResultCallback<Uri> imageActivityResultCallback = result -> {
        if (result == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(result)) {
            Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeStream(inputStream));
            openImage(bm);
            bm.recycle();

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
        } catch (NumberFormatException e) {
        }
        drawGridOnView();
    };

    private final DialogInterface.OnClickListener onNewImageDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int width = Integer.parseInt(etNewImageSizeX.getText().toString());
            int height = Integer.parseInt(etNewImageSizeY.getText().toString());
            newImage(width, height);
        } catch (NumberFormatException e) {
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canvas.drawPoint(toOriginal(x - translationX), toOriginal(y - translationY), eraser);
                drawBitmapOnView();
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                canvas.drawLine(
                        toOriginal(prevX - translationX),
                        toOriginal(prevY - translationY),
                        toOriginal(x - translationX),
                        toOriginal(y - translationY),
                        eraser);
                drawBitmapOnView();
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithEyedropperListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                paint.setColor(bitmap.getPixel(originalX, originalY));
                showPaintColorOnSeekBars();
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canvas.drawPoint(toOriginal(x - translationX), toOriginal(y - translationY), paint);
                drawBitmapOnView();
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                canvas.drawLine(
                        toOriginal(prevX - translationX),
                        toOriginal(prevY - translationY),
                        toOriginal(x - translationX),
                        toOriginal(y - translationY),
                        paint);
                drawBitmapOnView();
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithScalerListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1:
                float x = event.getX(), y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prevX = x;
                        prevY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        translationX += x - prevX;
                        translationY += y - prevY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
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
                        this.scale = scale;
                        imageWidth = scaledWidth;
                        imageHeight = scaledHeight;
                        float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        translationX = translationX - pivotX + this.pivotX;
                        translationY = translationY - pivotY + this.pivotY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        this.pivotX = pivotX;
                        this.pivotY = pivotY;
                        prevDiagonal = diagonal;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        this.pivotX = (x0 + x1) / 2.0f - translationX;
                        this.pivotY = (y0 + y1) / 2.0f - translationY;
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
                        break;
                }
                break;
        }
        return true;
    };

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void drawPoint(Canvas canvas, float x, float y, String text) {
        canvas.drawLine(x - 100.0f, y, x + 100.0f, y, pointPaint);
        canvas.drawLine(x, y - 100.0f, x, y + 100.0f, pointPaint);
        canvas.drawText(text, x, y, pointPaint);
    }

    private void drawBitmapOnView() {
        clearCanvas(viewCanvas);
        int startX = translationX >= 0.0f ? 0 : toOriginal(-translationX);
        int startY = translationY >= 0.0f ? 0 : toOriginal(-translationY);
        int endX = Math.min(toOriginal(translationX + imageWidth <= viewWidth ? imageWidth : viewWidth - translationX) + 1, bitmap.getWidth());
        int endY = Math.min(toOriginal(translationY + imageHeight <= viewHeight ? imageHeight : viewHeight - translationY) + 1, bitmap.getHeight());
        float left = translationX >= 0.0f ? translationX : translationX % scale;
        float top = translationY >= 0.0f ? translationY : translationY % scale;
        if (isScaledMuch()) {
            float t = top, b = t + scale;
            Paint paint = new Paint();
            for (int y = startY; y < endY; ++y, t += scale, b += scale) {
                float l = left;
                for (int x = startX; x < endX; ++x) {
                    paint.setColor(bitmap.getPixel(x, y));
                    viewCanvas.drawRect(l, t, l += scale, b, paint);
                }
            }
        } else {
            float right = Math.min(translationX + imageWidth, viewWidth);
            float bottom = Math.min(translationY + imageHeight, viewHeight);
            viewCanvas.drawBitmap(bitmap,
                    new Rect(startX, startY, endX, endY),
                    new RectF(left, top, right, bottom),
                    opaquePaint);
        }
        imageView.setImageBitmap(viewBitmap);
    }

    private void drawChessboardOnView() {
        clearCanvas(chessboardCanvas);
        float left = Math.max(0.0f, translationX);
        float top = Math.max(0.0f, translationY);
        float right = Math.min(translationX + imageWidth, viewWidth);
        float bottom = Math.min(translationY + imageHeight, viewHeight);
        chessboardCanvas.drawBitmap(chessboard,
                new Rect((int) left, (int) top, (int) right, (int) bottom),
                new RectF(left, top, right, bottom),
                opaquePaint);
        ivChessboard.setImageBitmap(chessboardBitmap);
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);
        float startX = translationX >= 0.0f ? translationX : translationX % scale;
        float startY = translationY >= 0.0f ? translationY : translationY % scale;
        float endX = Math.min(translationX + imageWidth, viewWidth);
        float endY = Math.min(translationY + imageHeight, viewHeight);
        if (isScaledMuch()) {
            for (float x = startX; x < endX; x += scale) {
                gridCanvas.drawLine(x, startY, x, endY, gridPaint);
            }
            for (float y = startY; y < endY; y += scale) {
                gridCanvas.drawLine(startX, y, endX, y, gridPaint);
            }
        }
        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 0) {
                float scaledSizeX = toScaled(cellGrid.sizeX);
                startX = translationX >= 0.0f ? translationX : translationX % scaledSizeX + toScaled(cellGrid.offsetX);
                for (float x = startX; x < endX; x += scaledSizeX) {
                    gridCanvas.drawLine(x, startY, x, endY, cellGridPaint);
                }
            }
            if (cellGrid.sizeY > 0) {
                float scaledSizeY = toScaled(cellGrid.sizeY);
                startY = translationY >= 0.0f ? translationY : translationY % scaledSizeY + toScaled(cellGrid.offsetY);
                for (float y = startY; y < endY; y += scaledSizeY) {
                    gridCanvas.drawLine(startX, y, endX, y, cellGridPaint);
                }
            }
        }
        ivGrid.setImageBitmap(gridBitmap);
    }

    private boolean isScaledMuch() {
        return bitmap.getWidth() / scale < 256 && bitmap.getHeight() / scale < 256;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();

        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        history = new BitmapHistory();
        history.offer(bitmap);

        scale = 20.0f;
        imageWidth = 960;
        imageHeight = 960;
        translationX = 0.0f;
        translationY = 0.0f;


        viewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        imageView.setImageBitmap(viewBitmap);

        gridBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        drawGridOnView();

        chessboardBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        chessboardCanvas = new Canvas(chessboardBitmap);
        drawChessboardOnView();
    }

    private void newImage(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        history = new BitmapHistory();
        history.offer(bitmap);

        scale = (float) ((double) imageView.getWidth() / (double) width);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);
        translationX = 0.0f;
        translationY = 0.0f;

        drawChessboardOnView();
        imageView.setImageBitmap(bitmap);
        drawGridOnView();
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
        ((RadioButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithEraserListener));
        ((RadioButton) findViewById(R.id.rb_eyedropper)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithEyedropperListener));
        ((RadioButton) findViewById(R.id.rb_pencil)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithPencilListener));
        ((RadioButton) findViewById(R.id.rb_scaler)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithScalerListener));
        sbAlpha.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etAlpha.setText(String.format("%02X", progress)));
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etBlue.setText(String.format("%02X", progress)));
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etGreen.setText(String.format("%02X", progress)));
        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etRed.setText(String.format("%02X", progress)));

        chessboard = BitmapFactory.decodeResource(getResources(), R.mipmap.chessboard);
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

            case R.id.i_new:
                AlertDialog newImageDialog = new AlertDialog.Builder(this)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", onNewImageDialogPosButtonClickListener)
                        .setTitle("New")
                        .setView(R.layout.new_image)
                        .show();

                etNewImageSizeX = newImageDialog.findViewById(R.id.et_new_size_x);
                etNewImageSizeY = newImageDialog.findViewById(R.id.et_new_size_y);

                bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                history = new BitmapHistory();
                history.offer(bitmap);
                break;

            case R.id.i_open:
                imageActivityResultLauncher.launch("image/*");
                break;

            case R.id.i_redo:
                if (history.canRedo()) {
                    clearCanvas(canvas);
                    canvas.drawBitmap(history.redo(), 0.0f, 0.0f, opaquePaint);
                    drawBitmapOnView();
                }
                break;

            case R.id.i_save:
                save();
                break;

            case R.id.i_undo:
                if (history.canUndo()) {
                    clearCanvas(canvas);
                    canvas.drawBitmap(history.undo(), 0.0f, 0.0f, opaquePaint);
                    drawBitmapOnView();
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

    private void openImage(Bitmap bitmap) {
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(this.bitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        history = new BitmapHistory();
        history.offer(this.bitmap);

        scale = (float) ((double) viewWidth / (double) width);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);
        translationX = 0.0f;
        translationY = 0.0f;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
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

    private int toOriginal(float scaled) {
        return (int) (scaled / scale);
    }

    private float toScaled(int original) {
        return original * scale;
    }
}