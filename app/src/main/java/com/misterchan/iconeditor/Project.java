package com.misterchan.iconeditor;

import android.graphics.Bitmap;

import com.waynejo.androidndkgif.GifEncoder;

import java.util.ArrayList;
import java.util.List;

public class Project {
    public boolean gifDither = true;
    public Bitmap.CompressFormat compressFormat;
    public final List<Frame> frames = new ArrayList<>();
    public GifEncoder.EncodingType gifEncodingType;
    public Tab.FileType fileType;
    public Tab.Filter filter;
    public int quality = -1;
    public String filePath;
}
