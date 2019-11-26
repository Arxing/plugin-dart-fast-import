package org.arxing.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class LibInfo {
    String pkgName;
    File libRootFile;
    LibType type;

    public LibInfo(String libName, File rootFile, LibType type) {
        this.pkgName = libName;
        this.type = type;
        this.libRootFile = rootFile;
    }

    public List<LibTarget> getAllTargets() {
        List<LibTarget> result = new ArrayList<>();
        switch (type) {
            case dart:
                result.add(LibTarget.ofDart(this, pkgName));
                break;
            case packages:
            case file:
                visitChildInternal(result, false, libRootFile);
                break;
        }
        return result;
    }

    private void visitChildInternal(List<LibTarget> result, boolean recursive, File currentFile) {
        //                System.out.println("拜訪:" + currentFile.toPath());
        resolveTarget(result, currentFile);
        if (currentFile.isDirectory()) {
            if (recursive) {
                for (File file : currentFile.listFiles()) {
                    visitChildInternal(result, recursive, file);
                }
            } else {
                for (File file : currentFile.listFiles()) {
                    resolveTarget(result, file);
                }
            }
        }
    }

    private void resolveTarget(List<LibTarget> result, File file) {
        URI absoluteUri = libRootFile.toURI();
        URI relativeUri = absoluteUri.relativize(file.toURI());
        switch (type) {
            case file:
                LibTarget fileTarget = LibTarget.ofFile(this, relativeUri, absoluteUri, file.isDirectory());
                result.add(fileTarget);
            case packages:
                LibTarget pkgTarget = LibTarget.ofPackage(this, pkgName, relativeUri, absoluteUri, file.isDirectory());
                result.add(pkgTarget);
                break;
        }
    }
}
