package com.misterchan.iconeditor.ui;

import android.graphics.Color;

import com.misterchan.iconeditor.Project;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    private List<Long> palette;
    private List<Project> projects;

    public List<Long> getPalette() {
        if (palette == null) {
            palette = new ArrayList<>() {
                {
                    add(Color.pack(Color.BLACK));
                    add(Color.pack(Color.WHITE));
                    add(Color.pack(Color.RED));
                    add(Color.pack(Color.YELLOW));
                    add(Color.pack(Color.GREEN));
                    add(Color.pack(Color.CYAN));
                    add(Color.pack(Color.BLUE));
                    add(Color.pack(Color.MAGENTA));
                }
            };
        }
        return palette;
    }

    public List<Project> getProjects() {
        if (projects == null) {
            projects = new ArrayList<>();
        }
        return projects;
    }
}
