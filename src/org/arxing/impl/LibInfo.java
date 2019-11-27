package org.arxing.impl;

import com.annimon.stream.Stream;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class LibInfo {
    private URI workFileUri;
    private String pkgName;
    private File libRootFile;
    private LibType type;

    public LibInfo(URI workFileUri, String libName, File rootFile, LibType type) {
        this.workFileUri = workFileUri;
        this.pkgName = libName;
        this.type = type;
        this.libRootFile = rootFile;
    }

    public URI getWorkFileUri() {
        return workFileUri;
    }

    public URI getLibRootUri() {
        return libRootFile.toURI();
    }

    public List<LibTarget> getAllTargets(boolean recursive) {
        List<LibTarget> result = new ArrayList<>();
        switch (type) {
            case dart:
                result.add(LibTarget.ofDart(this, pkgName));
                break;
            case packages:
            case file:
                visitChildInternal(result, recursive, libRootFile);
                break;
        }
        return result;
    }

    private void visitChildInternal(List<LibTarget> result, boolean recursive, File currentFile) {
        resolveTarget(result, currentFile);
        if (currentFile.isDirectory()) {
            Stream.ofNullable(currentFile.listFiles())
                  .filter(file -> file.isDirectory() || (file.isFile() && file.getName().endsWith(".dart")))
                  .forEach(file -> {
                      if (recursive)
                          visitChildInternal(result, recursive, file);
                      else
                          resolveTarget(result, file);
                  });
        }
    }

    private void resolveTarget(List<LibTarget> result, File file) {
        URI libRootUri = libRootFile.toURI();
        URI relativeUri = libRootUri.relativize(file.toURI());
        switch (type) {
            case file:
                LibTarget fileTarget = LibTarget.ofFile(this, libRootUri, relativeUri, libRootUri, file.isDirectory());
                result.add(fileTarget);
            case packages:
                LibTarget pkgTarget = LibTarget.ofPackage(this, pkgName, libRootUri, relativeUri, libRootUri, file.isDirectory());
                result.add(pkgTarget);
                break;
        }
    }
}
