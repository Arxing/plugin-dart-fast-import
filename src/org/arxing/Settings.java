package org.arxing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

public interface Settings {
    static Settings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, Settings.class);
    }

    void setRecursive(boolean recursive);

    boolean isRecursive();
}
