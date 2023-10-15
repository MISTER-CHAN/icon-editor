package com.misterchan.iconeditor.ui;

import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.Project;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    private LinkedList<Long> palette;
    private List<Project> projects;

    public LinkedList<Long> getPalette() {
        if (palette == null) {
            palette = new LinkedList<>() {
                {
                    offer(Color.pack(Color.BLACK));
                    offer(Color.pack(Color.WHITE));
                    offer(Color.pack(Color.RED));
                    offer(Color.pack(Color.YELLOW));
                    offer(Color.pack(Color.GREEN));
                    offer(Color.pack(Color.CYAN));
                    offer(Color.pack(Color.BLUE));
                    offer(Color.pack(Color.MAGENTA));
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
