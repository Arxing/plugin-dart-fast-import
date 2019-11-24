package org.arxing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DependencyAnalyzer {

    static DependencyAnalyzer getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, DependencyAnalyzer.class);
    }

    List<String> filterDependencies(String target);

    void updateDependencies() throws Exception;

}
