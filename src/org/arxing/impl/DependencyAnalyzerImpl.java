package org.arxing.impl;

import org.arxing.DependencyAnalyzer;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependencyAnalyzerImpl implements DependencyAnalyzer {
    private Project project;
    private Map<String, LibInfo> libs = new HashMap<>();
    private List<LibTarget> dependenciesCache = new ArrayList<>();
    private static String[] dartPackages = {
            "typed_data", "io", "collection", "convert", "async", "developer", "ffi", "isolate", "math", "nativewrappers", "ui", "core",
    };

    public DependencyAnalyzerImpl(Project project) throws Exception {
        this.project = project;
    }

    @Override public List<LibTarget> getDependencies() {
        return dependenciesCache;
    }

    @Override public void updateDependencies() throws Exception {
        VirtualFile pkgFile = project.getBaseDir().findChild(".packages");
        List<String> lines = FileUtil.loadLines(pkgFile.getPath())
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
                rootFile = new File(project.getBasePath(), libPath);
                type = LibType.file;
            }
            LibInfo libInfo = new LibInfo(libName, rootFile, type);
            libs.put(libName, libInfo);
        }
        for (String dartPackage : dartPackages) {
            libs.put(dartPackage, new LibInfo(dartPackage, null, LibType.dart));
        }
        dependenciesCache.clear();
        dependenciesCache.addAll(Stream.of(libs.values()).flatMap(info -> Stream.of(info.getAllTargets())).toList());
    }
}
