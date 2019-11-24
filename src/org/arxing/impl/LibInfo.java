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
                result.add(LibTarget.ofDart(pkgName));
                break;
            case packages:
            case file:
                visitChildInternal(result, true, libRootFile);
                break;
        }
        return result;
    }

    private void visitChildInternal(List<LibTarget> result, boolean recursive, File currentFile) {
        //        System.out.println("拜訪:" + currentFile.toPath());
        if (currentFile.isFile()) {
            result.add(buildTarget(currentFile));
        } else if (currentFile.isDirectory()) {
            if (recursive) {
                for (File file : currentFile.listFiles()) {
                    visitChildInternal(result, recursive, file);
                }
            } else {
                for (File file : currentFile.listFiles()) {
                    LibTarget libTarget = buildTarget(file);
                    result.add(libTarget);
                }
            }
        }
    }

    private LibTarget buildTarget(File file) {
        String relative = libRootFile.toPath().relativize(file.toPath()).toString().replace("\\", "/");
        switch (type) {
            case packages:
                return LibTarget.ofPackage(pkgName, URI.create(relative));
            case file:
                return LibTarget.ofFile(URI.create(relative));
        }
        throw new IllegalStateException();
    }

}
