package org.arxing.impl;

import java.net.URI;

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
