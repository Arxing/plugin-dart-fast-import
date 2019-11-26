package org.arxing.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.arxing.Printer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LibTarget implements Comparable<LibTarget> {
    private LibInfo context;
    private LibType type;
    private String pkgName;
    private URI libRootUri;
    private URI absoluteUri;
    private URI relativeUri;
    private boolean isDirectory;

    private LibTarget(LibInfo context,
                      LibType type,
                      String pkgName,
                      URI libRootUri,
                      URI relativeUri,
                      URI absoluteUri,
                      boolean isDirectory) {
        this.context = context;
        this.type = type;
        this.pkgName = pkgName;
        this.libRootUri = libRootUri;
        this.relativeUri = relativeUri;
        this.isDirectory = isDirectory;
        this.absoluteUri = absoluteUri;
    }

    public static LibTarget ofDart(LibInfo context, String dartLib) {
        return new LibTarget(context, LibType.dart, dartLib, null, null, null, false);
    }

    public static LibTarget ofPackage(LibInfo context,
                                      String pkgName,
                                      URI libRootUri,
                                      URI relativeUri,
                                      URI absoluteUri,
                                      boolean isDirectory) {
        return new LibTarget(context, LibType.packages, pkgName, libRootUri, relativeUri, absoluteUri, isDirectory);
    }

    public static LibTarget ofFile(LibInfo context, URI libRootUri, URI relativeUri, URI absoluteUri, boolean isDirectory) {
        return new LibTarget(context, LibType.file, null, libRootUri, relativeUri, absoluteUri, isDirectory);
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

    public boolean isLeaf() {
        return !relativeUri.getPath().endsWith("/") && !relativeUri.getPath().isEmpty();
    }

    private Predicate<File> dartFileFilter = file -> file.isDirectory() || (file.isFile() && file.getName().endsWith(".dart"));

    public List<LibTarget> getRelationTargets() {
        List<LibTarget> result = new ArrayList<>();
        URI targetUri = absoluteUri.resolve(relativeUri);
        File targetFile = new File(targetUri);
        switch (type) {
            case file:
            case packages:
                File[] children = targetFile.listFiles();
                Stream.ofNullable(children)
                      .filter(dartFileFilter)
                      .filterNot(file -> file.getPath().equals(targetFile.getPath()))
                      .forEach(child -> {
                          URI relativeUri = targetUri.relativize(child.toURI());
                          if (type == LibType.file)
                              result.add(LibTarget.ofFile(context, libRootUri, relativeUri, targetUri, child.isDirectory()));
                          else
                              result.add(LibTarget.ofPackage(context, pkgName, libRootUri, relativeUri, targetUri, child.isDirectory()));
                      });
                break;
            case dart:
                break;
        }
        return result;
    }

    private String handleDirectorySuffix(String src) {
        if (isDirectory && !src.endsWith("/"))
            return src + "/";
        return src;
    }

    private String computeRelativePathAsLibRoot() {
        return libRootUri.relativize(absoluteUri.resolve(relativeUri)).getPath();
    }

    @Override public String toString() {
        switch (type) {
            case dart:
                return "dart:" + pkgName;
            case packages:
                return handleDirectorySuffix(String.format("package:%s/%s", pkgName, computeRelativePathAsLibRoot()));
            case file:
                return handleDirectorySuffix(computeRelativePathAsLibRoot());
        }
        return super.toString();
    }

    @Override public int compareTo(@NotNull LibTarget o) {
        if (this.type != o.type)
            return this.type.compareTo(o.type);
        List<Integer> sorts = new ArrayList<>();
        if (type != LibType.file)
            sorts.add(this.pkgName.compareTo(o.pkgName));
        if (type != LibType.dart)
            sorts.add(this.computeRelativePathAsLibRoot().compareTo(o.computeRelativePathAsLibRoot()));
        return Stream.of(sorts).filterNot(sort -> sort == 0).findFirst().orElse(0);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LibTarget target = (LibTarget) o;
        return type == target.type && Objects.equals(pkgName, target.pkgName) && Objects.equals(libRootUri,
                                                                                                target.libRootUri) && Objects.equals(
                absoluteUri,
                target.absoluteUri) && Objects.equals(relativeUri, target.relativeUri);
    }

    @Override public int hashCode() {
        return Objects.hash(type, pkgName, libRootUri, absoluteUri, relativeUri);
    }
}
