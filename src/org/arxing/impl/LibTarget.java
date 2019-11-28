package org.arxing.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.sun.javaws.exceptions.ErrorCodeResponseException;

import org.arxing.Printer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("Duplicates")
public class LibTarget implements Comparable<LibTarget> {
    private LibInfo context;
    private LibType type;
    private String pkgName;
    private URI libRootUri;
    private URI absoluteUri;
    private URI relativeUri;
    private URI projectRoot;
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
        this.projectRoot = context.getWorkFileUri();
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

    public LibType getType() {
        return type;
    }

    public boolean isLeaf() {
        if (type == LibType.dart)
            return true;
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
        src = src.replace("\\", "/");
        if (isDirectory && !src.endsWith("/"))
            return src + "/";
        return src;
    }

    private String computeRelativePathAsLibRoot() {
        return libRootUri.relativize(absoluteUri.resolve(relativeUri)).getPath();
    }

    private void handleSegment(List<PairSegment> segments, String wordSegment) {
        String[] splits = wordSegment.split("_");
        for (int i = 0; i < splits.length; i++) {
            String seg = splits[i];
            segments.add(new PairSegment(seg, true));
            if (i == splits.length - 1)
                segments.add(new PairSegment("_", false));
        }
        if (!wordSegment.endsWith("dart"))
            segments.add(new PairSegment("/", false));
    }

    private List<PairSegment> collectSegments() {
        List<PairSegment> segments = new ArrayList<>();
        switch (type) {
            case file:
                calcPath().iterator().forEachRemaining(path -> {
                    String s = path.toString();
                    if (s.isEmpty())
                        return;
                    handleSegment(segments, s);
                });
                break;
            case packages:
                segments.add(new PairSegment("package", true));
                segments.add(new PairSegment(":", false));
                segments.add(new PairSegment(pkgName, true));
                segments.add(new PairSegment("/", false));
                calcPath().iterator().forEachRemaining(path -> {
                    String s = path.toString();
                    if (s.isEmpty())
                        return;
                    handleSegment(segments, s);
                });
                break;
            case dart:
                segments.add(new PairSegment("dart", true));
                segments.add(new PairSegment(":", false));
                segments.add(new PairSegment(pkgName, true));
                break;
        }
        return segments;
    }

    public String calcRegex1() {
        List<String> fragments = Stream.of(collectSegments()).map(o -> o.segment).toList();
        String regex = "";
        if (fragments.size() > 1) {
            regex = Stream.of(fragments).collect(Collectors.joining("("));
            regex += Stream.range(0, fragments.size() - 1).map(o -> ")?").collect(Collectors.joining());
        } else if (fragments.size() == 1) {
            regex = fragments.get(0);
        }
        return regex;
    }

    public String calcRegex2() {
        String m = Stream.of(collectSegments()).map(pair -> String.format("(%s)?", pair.segment)).collect(Collectors.joining());
        Printer.print("\"%s\"的正則=\"%s\"", toString(), m);
        return m;
    }

    public static String wrapSegment(String segment) {
        String r = "";
        if (segment.length() > 1) {
            r = Stream.of(segment.split("")).collect(Collectors.joining("("));
            r += Stream.range(0, segment.length() - 1).map(o -> ")?").collect(Collectors.joining());
        } else if (segment.length() == 1) {
            r = segment;
        }
        return r;
    }

    private Path calcPath() {
        switch (type) {
            case file:
                Path workFilePath = new File(context.getWorkFileUri()).toPath();
                Path libFilePath = new File(absoluteUri.resolve(relativeUri)).toPath();
                String finalPath = workFilePath.relativize(libFilePath).toString();
                if (finalPath.startsWith("..\\")) {
                    finalPath = finalPath.substring(3);
                }
                return new File(finalPath).toPath();
            case packages:
                return new File(computeRelativePathAsLibRoot()).toPath();
            case dart:
                break;
        }
        return null;
    }

    @Override public String toString() {
        switch (type) {
            case dart:
                return "dart:" + pkgName;
            case packages:
                return handleDirectorySuffix(String.format("package:%s/%s", pkgName, computeRelativePathAsLibRoot()));
            case file:
                Path workFilePath = new File(context.getWorkFileUri()).toPath();
                Path libFilePath = new File(absoluteUri.resolve(relativeUri)).toPath();
                String finalPath = workFilePath.relativize(libFilePath).toString();
                if (finalPath.startsWith("..\\")) {
                    finalPath = finalPath.substring(3);
                }
                return handleDirectorySuffix(finalPath);
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

    private static class PairSegment {
        String segment;
        boolean handle;

        public PairSegment(String segment, boolean handle) {
            this.handle = handle;
            this.segment = handle ? wrapSegment(segment) : segment;
            this.segment = this.segment.replace(".", "\\.");
        }
    }
}
