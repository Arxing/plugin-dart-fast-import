package org.arxing;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntPair;
import com.annimon.stream.Stream;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import org.apache.commons.io.FileUtils;
import org.arxing.ui.MainDialog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ImportAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(ImportAction.class);
    private Project project;

    @Override public void actionPerformed(AnActionEvent e) {
        project = e.getData(DataKeys.PROJECT);
        VirtualFile file = e.getData(DataKeys.VIRTUAL_FILE);

        try {
            MainDialog mainDialog = new MainDialog(project, file, false);
            mainDialog.setListener((kind, target) -> {
                String finalTarget = kind + " '" + target + "';";
                List<String> lines = FileUtil.loadLines(file.getPath());
                int insertLine = -1;
                switch (kind) {
                    case "import":
                    case "export":
                        insertLine = Stream.of(lines).anyMatch(line -> line.contains(target)) ? -1 : 0;
                        break;
                    case "part":
                    case "part of":
                        insertLine = Stream.of(lines)
                                           .indexed()
                                           .filter(pair -> pair.getSecond().matches("import '.+';"))
                                           .map(stringIntPair -> stringIntPair.getFirst() + 1)
                                           .findLast()
                                           .orElse(0);
                        break;
                }
                if (insertLine != -1)
                    lines.add(insertLine, finalTarget);
                FileUtils.writeLines(new File(file.getPath()), lines);
                mainDialog.setVisible(false);
                file.refresh(false, false);
            });
            mainDialog.setVisible(true);
        } catch (Throwable e1) {
            Messages.showErrorDialog(Stream.of(Arrays.asList(e1.getStackTrace()))
                                           .map(StackTraceElement::toString)
                                           .collect(com.annimon.stream.Collectors.joining("\n")), "title-exception");
        }
    }

    @Override public void update(AnActionEvent e) {
        super.update(e);
    }
}
