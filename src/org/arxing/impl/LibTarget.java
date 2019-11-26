package org.arxing.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.arxing.Printer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LibTarget implements Comparable<LibTarget> {
    private LibInfo context;
    private LibType type;
    private String pkgName;
    private URI relativeUri;
    private boolean isDirectory;
    private URI absoluteUri;

    private LibTarget(LibInfo context, LibType type, String pkgName, URI relativeUri, URI absoluteUri, boolean isDirectory) {
        this.context = context;
        this.type = type;
        this.pkgName = pkgName;
        this.relativeUri = relativeUri;
        this.isDirectory = isDirectory;
        this.absoluteUri = absoluteUri;
    }

    public static LibTarget ofDart(LibInfo context, String dartLib) {
        return new LibTarget(context, LibType.dart, dartLib, null, null, false);
    }

    public static LibTarget ofPackage(LibInfo context, String pkgName, URI relativeUri, URI absoluteUri, boolean isDirectory) {
        return new LibTarget(context, LibType.packages, pkgName, relativeUri, absoluteUri, isDirectory);
    }

    public static LibTarget ofFile(LibInfo context, URI relativeUri, URI absoluteUri, boolean isDirectory) {
        return new LibTarget(context, LibType.file, null, relativeUri, absoluteUri, isDirectory);
    }

    private String backPath(String path) {
        List<String> splits = Stream.of(path.split("/")).toList();
        splits = Stream.of(splits).limit(splits.size() - 1).toList();
        return Stream.of(splits).collect(Collectors.joining("/"));
    }

    public String backTarget() {
        switch (type) {
            case dart:
                return "dart:";
            case packages:
                return "package:" + pkgName + "/" + backPath(relativeUri.getPath());
            case file:
                return backPath(relativeUri.getPath());
        }
        return null;
    }

    public List<LibTarget> getRelationTargets() {
        List<LibTarget> result = new ArrayList<>();
        URI targetUri = absoluteUri.resolve(relativeUri);
        File targetFile = new File(targetUri);
        URI relativeUri;
        if (type == LibType.file || type == LibType.packages) {
            if (targetFile.isFile()) {
                for (File child : targetFile.getParentFile().listFiles()) {
                    // 略過自己
                    if (child.toURI().equals(targetUri))
                        continue;
                    relativeUri = absoluteUri.relativize(child.toURI());

                    if (type == LibType.file)
                        result.add(LibTarget.ofFile(context, relativeUri, targetUri, child.isDirectory()));
                    result.add(LibTarget.ofPackage(context, context.pkgName, relativeUri, targetUri, child.isDirectory()));
                }
            } else {
                for (File child : targetFile.listFiles()) {
                    relativeUri = targetUri.relativize(child.toURI());
                    if (type == LibType.file)
                        result.add(LibTarget.ofFile(context, relativeUri, targetUri, child.isDirectory()));
                    else
                        result.add(LibTarget.ofPackage(context, context.pkgName, relativeUri, targetUri, child.isDirectory()));
                }
            }
        }
        return result;
    }


    private String handleDirectorySuffix(String src) {
        if (isDirectory && !src.endsWith("/"))
            return src + "/";
        return src;
    }

    @Override public String toString() {
        switch (type) {
            case dart:
                return "dart:" + pkgName;
            case packages:
                return handleDirectorySuffix(String.format("package:%s/%s", pkgName, relativeUri.getPath()));
            case file:
                return handleDirectorySuffix(relativeUri.getPath());
        }
        return super.toString();
    }

    @Override public int compareTo(@NotNull LibTarget o) {
        List<Integer> sorts = new ArrayList<>();
        sorts.add(this.type.compareTo(o.type));
        sorts.add(this.pkgName == null || o.pkgName == null ? 0 : this.pkgName.compareTo(o.pkgName));
        sorts.add(this.relativeUri == null || o.relativeUri == null ? 0 : this.relativeUri.compareTo(o.relativeUri));
        int r = Stream.of(sorts).filterNot(sort -> sort == 0).findFirst().orElse(0);
        return r;
    }
}
