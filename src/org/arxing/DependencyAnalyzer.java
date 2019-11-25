package org.arxing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import org.arxing.impl.LibInfo;
import org.arxing.impl.LibTarget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DependencyAnalyzer {

    static DependencyAnalyzer getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, DependencyAnalyzer.class);
    }

    List<LibTarget> getDependencies();

    void updateDependencies() throws Exception;

}
