package org.arxing.impl;

import org.arxing.DependencyAnalyzer;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependencyAnalyzerImpl implements DependencyAnalyzer {
    private static String[] dartPackages = {
            "typed_data", "io", "collection", "convert", "async", "developer", "ffi", "isolate", "math", "nativewrappers", "ui", "core",
    };
    private Project project;
    private List<LibInfo> libs = new ArrayList<>();
    private List<LibTarget> dependenciesCache = new ArrayList<>();

    public DependencyAnalyzerImpl(Project project) {
        this.project = project;
    }

    @Override public List<LibTarget> getDependencies(String keyword) {
        return Stream.of(dependenciesCache).filter(target -> {
            if (keyword == null || keyword.isEmpty())
                return true;
            return target.toString().contains(keyword);
        }).toList();
    }

    @Override public void updateDependencies() throws Exception {
        List<String> lines = FileUtil.loadLines(getPackagesFilePath())
                                     .stream()
                                     .filter(line -> !line.startsWith("#"))
                                     .collect(Collectors.toList());
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
        dependenciesCache.sort(Comparator.naturalOrder());
    }

    private String getPackagesFilePath() {
        return new File(getProjectRootPath(), ".packages").getPath();
    }

    @Override public String getProjectRootPath() {
        if (project != null)
            return project.getBasePath();
        else
            return "W:\\flutter\\e7_backend";
    }

    /**
     * 根據關鍵字 動態的從緩存的庫中搜尋出匹配的目標
     *
     * @param keyword
     * @return
     */
    @Override public List<LibTarget> matchTargets(String keyword) {
        List<LibTarget> targets = new ArrayList<>();

        return null;
    }
}
