package org.arxing.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.net.URI;
import java.util.List;

public class LibTarget {
    private LibType type;
    private String pkgName;
    private URI uri;

    private LibTarget(LibType type, String pkgName, URI uri) {
        this.type = type;
        this.pkgName = pkgName;
        this.uri = uri;
    }

    public static LibTarget ofDart(String dartLib) {
        return new LibTarget(LibType.dart, dartLib, null);
    }

    public static LibTarget ofPackage(String pkgName, URI uri) {
        return new LibTarget(LibType.packages, pkgName, uri);
    }

    public static LibTarget ofFile(URI uri) {
        return new LibTarget(LibType.file, null, uri);
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
                return "package:" + pkgName + "/" + backPath(uri.getPath());
            case file:
                return backPath(uri.getPath());
        }
        return null;
    }

    @Override public String toString() {
        switch (type) {
            case dart:
                return "dart:" + pkgName;
            case packages:
                return "package:" + pkgName + "/" + uri.getPath();
            case file:
                return uri.getPath();
        }
        return super.toString();
    }
}
