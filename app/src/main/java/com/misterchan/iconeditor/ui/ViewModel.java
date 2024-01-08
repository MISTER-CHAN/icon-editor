package com.misterchan.iconeditor.ui;

import android.graphics.Color;

import com.misterchan.iconeditor.Project;

import java.util.ArrayList;
import java.util.List;

public class ViewModel extends androidx.lifecycle.ViewModel {
    private List<Project> projects;

    public List<Project> getProjects() {
        if (projects == null) {
            projects = new ArrayList<>();
        }
        return projects;
    }
}
