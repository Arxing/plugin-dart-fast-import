package org.arxing.impl;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import org.arxing.DependencyAnalyzer;
import org.arxing.Settings;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyAnalyzerImpl implements DependencyAnalyzer {
    private static String[] DART_PACKAGES = {
            "typed_data", "io", "collection", "convert", "async", "developer", "ffi", "isolate", "math", "nativewrappers", "ui", "core",
    };
    private static String TEST_PATH_ROOT = "W:\\flutter\\platform51_core";
    private Project project;
    private List<LibInfo> libs = new ArrayList<>();
    private Set<LibTarget> dependenciesCache = new HashSet<>();
    private Settings settings;
    private String workFilePath;
    private Map<String, String> regex1Cache = new HashMap<>();
    private Map<String, String> regex2Cache = new HashMap<>();

    public DependencyAnalyzerImpl(Project project) {
        if (project != null) {
            this.project = project;
            settings = Settings.getInstance(project);
        } else {
            settings = new SettingsImpl(null);
            workFilePath = "W:\\flutter\\platform51_core\\lib\\business\\business_logic.dart";
        }
    }

    private Predicate<LibTarget> keywordFilter(String keyword) {
        return target -> {
            String s = target.toString();
            if (s.equals(keyword))
                return true;
            if (s.contains(keyword))
                return true;
            if (!regex2Cache.containsKey(s))
                regex2Cache.put(s, target.calcRegex2());
            if (keyword.matches(regex2Cache.get(s)))
                return true;
            if (!regex1Cache.containsKey(s))
                regex1Cache.put(s, target.calcRegex1());
            if (keyword.matches(regex1Cache.get(s)))
                return true;
            return false;
        };
    }

    private URI getWorkFileUri() {
        return new File(workFilePath).toURI();
    }

    @Override public List<LibTarget> getDependencies(List<LibType> types, String keyword) {
        if (keyword == null || keyword.isEmpty())
            return Stream.of(dependenciesCache).filter(target -> types == null || types.contains(target.getType())).toList();
        return Stream.of(dependenciesCache)
                     .filter(target -> types == null || types.contains(target.getType()))
                     .filter(keywordFilter(keyword))
                     .toList();
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
            LibInfo libInfo = new LibInfo(getWorkFileUri(), libName, rootFile, type);
            libs.add(libInfo);
        }
        for (String dartPackage : DART_PACKAGES) {
            libs.add(new LibInfo(getWorkFileUri(), dartPackage, null, LibType.dart));
        }
        dependenciesCache.clear();
        dependenciesCache.addAll(Stream.of(libs).flatMap(info -> Stream.of(info.getAllTargets(settings.isRecursive()))).toList());
    }

    private String getPackagesFilePath() {
        return new File(getProjectRootPath(), ".packages").getPath();
    }

    @Override public String getProjectRootPath() {
        if (project != null)
            return project.getBasePath();
        else
            return TEST_PATH_ROOT;
    }

    @Override public void putExtraDependencies(List<LibTarget> extras) {
        if (!settings.isRecursive())
            dependenciesCache.addAll(extras);
    }

    @Override public void setWorkFilePath(String path) {
        this.workFilePath = path;
    }
}
