package org.arxing.impl;

import org.arxing.Settings;

import com.intellij.openapi.project.Project;

public class SettingsImpl implements Settings {
    boolean recursive = true;

    public SettingsImpl(Project project) {

    }

    @Override public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    @Override public boolean isRecursive() {
        return recursive;
    }
}
