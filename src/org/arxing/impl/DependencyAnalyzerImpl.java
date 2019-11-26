package org.arxing.impl;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import org.arxing.DependencyAnalyzer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyAnalyzerImpl implements DependencyAnalyzer {
    private static String[] dartPackages = {
            "typed_data", "io", "collection", "convert", "async", "developer", "ffi", "isolate", "math", "nativewrappers", "ui", "core",
    };
    private Project project;
    private List<LibInfo> libs = new ArrayList<>();
    private Set<LibTarget> dependenciesCache = new HashSet<>();

    public DependencyAnalyzerImpl(Project project) {
        this.project = project;
    }

    private Predicate<LibTarget> keywordFilter(String keyword) {
        return target -> {
            return target.toString().contains(keyword);
        };
    }

    @Override public List<LibTarget> getDependencies(String keyword) {
        if (keyword == null || keyword.isEmpty())
            return Stream.of(dependenciesCache).toList();
        return Stream.of(dependenciesCache).filter(keywordFilter(keyword)).toList();
    }

    @Override public void updateDependencies() throws Exception {
        List<String> lines = Stream.of(FileUtil.loadLines(getPackagesFilePath())).filter(line -> !line.startsWith("#")).toList();
        libs.clear();
        for (String line : lines) {
            String[] splits = line.split(":", 2);
            String libName = splits[0];
            String libPath = splits[1];
            File rootFile;
            LibType type;
            if (libPath.startsWith("file:///")) {
                rootFile = new File(URI.create(libPath));
                type = LibType.packages;
            } else {
                rootFile = new File(getProjectRootPath(), libPath);
                type = LibType.file;
            }
            LibInfo libInfo = new LibInfo(libName, rootFile, type);
            libs.add(libInfo);
        }
        for (String dartPackage : dartPackages) {
            libs.add(new LibInfo(dartPackage, null, LibType.dart));
        }
        dependenciesCache.clear();
        dependenciesCache.addAll(Stream.of(libs).flatMap(info -> Stream.of(info.getAllTargets())).toList());
    }

    private String getPackagesFilePath() {
        return new File(getProjectRootPath(), ".packages").getPath();
    }

    @Override public String getProjectRootPath() {
        if (project != null)
            return project.getBasePath();
        else
            return "W:\\flutter\\Platform51App-Core";
    }

    @Override public void putExtraDependencies(List<LibTarget> extras) {
        dependenciesCache.addAll(extras);
    }
}
