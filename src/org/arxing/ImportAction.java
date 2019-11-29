package org.arxing;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import org.arxing.ui.LibDialog;

import java.io.File;
import java.util.List;

public class ImportAction extends AnAction {
    private Project project;
    private VirtualFile targetFile;
    private LibDialog dialog;
    private DependencyAnalyzer dependencyAnalyzer;

    @Override public void actionPerformed(AnActionEvent e) {
        try {
            project = e.getData(DataKeys.PROJECT);
            dependencyAnalyzer = DependencyAnalyzer.getInstance(project);
            targetFile = e.getData(DataKeys.VIRTUAL_FILE);
            dependencyAnalyzer.setWorkFilePath(targetFile.getPath());
            if (targetFile == null || targetFile.getExtension() == null || !targetFile.getExtension().equals("dart"))
                return;
            dialog = new LibDialog(project);
            dialog.setCallback(dialogCallback);
            dialog.setVisible(true);
        } catch (Throwable e1) {
            handleError(e1);
        }
    }

    private void handleError(Throwable t) {
        StringBuilder builder = new StringBuilder(t.getMessage());
        builder.append(Stream.of(t.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
        Messages.showErrorDialog(builder.toString(), "Dart Fast Import Exception");
    }

    private void saveFile() {
        FileDocumentManager manager = FileDocumentManager.getInstance();
        Document document = manager.getDocument(targetFile);
        if (document != null)
            manager.saveDocument(document);
    }

    private LibDialog.LibDialogCallback dialogCallback = (importsType, path) -> {
        try {
            handleAction(new File(targetFile.getPath()), importsType, path);
            if (dialog != null)
                dialog.setVisible(false);
            targetFile.refresh(false, false);
        } catch (Exception e) {
            handleError(e);
        }
    };

    private void handleAction(File targetFile, ImportsType importsType, String path) throws Exception {
        List<String> lines = FileUtil.loadLines(targetFile, "utf-8");
        int insertLine = -1;
        switch (importsType) {
            case IMPORT:
                insertLine = Stream.of(lines)
                                   .indexed()
                                   .filter(pair -> pair.getSecond().matches("import '.+';"))
                                   .map(pair -> pair.getFirst() + 1)
                                   .findLast()
                                   .orElse(0);
                break;
            case EXPORT:
                insertLine = Stream.of(lines)
                                   .indexed()
                                   .filter(pair -> pair.getSecond().matches("export '.+';"))
                                   .map(pair -> pair.getFirst() + 1)
                                   .findLast()
                                   .orElse(0);
                break;
            case PART:
                insertLine = Stream.of(lines).indexed().filter(pair -> {
                    String line = pair.getSecond();
                    return line.matches("import '.+';") || line.matches("export '.+';") || line.matches("part '.+';");
                }).map(pair -> pair.getFirst() + 1).findLast().orElse(0);
                break;
            case PART_OF:
                insertLine = Stream.of(lines).indexed().filter(pair -> {
                    String line = pair.getSecond();
                    return line.matches("import '.+';") || line.matches("export '.+';") || line.matches("part of '.+';");
                }).map(pair -> pair.getFirst() + 1).findLast().orElse(0);
                break;
        }
        if (insertLine != -1) {
            String insertContent = String.format("%s '%s';", importsType.getOption(), path);
            if (lines.contains(insertContent))
                return;

            lines.add(insertLine, insertContent);
            String newFileContent = Stream.of(lines).collect(Collectors.joining("\n"));
            saveFile();
            FileUtil.writeToFile(targetFile, newFileContent);
        }
    }
}
