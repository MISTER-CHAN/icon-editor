package com.misterchan.iconeditor;

import android.graphics.Bitmap;

import com.google.android.material.tabs.TabLayout;
import com.misterchan.iconeditor.ui.FrameAdapter;
import com.waynejo.androidndkgif.GifEncoder;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Project {
    public enum FileType {
        PNG, JPEG, GIF, WEBP
    }

    public boolean gifDither = true;
    public Bitmap.CompressFormat compressFormat;
    public final CellGrid cellGrid = new CellGrid();
    public float scale = Float.NaN;
    public float translationX = Float.NaN, translationY = Float.NaN;
    public FrameAdapter frameAdapter = new FrameAdapter(this);
    public FileType fileType;
    public GifEncoder.EncodingType gifEncodingType;
    public final Deque<Guide> guides = new LinkedList<>();
    public final History history = new History();
    public int onionSkins = 1;
    public int quality = -1;
    public int selectedFrameIndex = 0;
    public final List<Frame> frames = new ArrayList<>();
    public String filePath;
    private String title;
    public TabLayout.Tab tab;

    public Frame getFirstFrame() {
        return frames.get(0);
    }

    public String getName() {
        final int i = title.lastIndexOf('.');
        return i == -1 ? title : title.substring(0, i);
    }

    public Frame getSelectedFrame() {
        return frames.get(selectedFrameIndex);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (tab != null) {
            tab.setText(title);
        }
    }
}
