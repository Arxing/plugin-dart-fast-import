package org.arxing;

import com.annimon.stream.Stream;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

import org.arxing.ui.MainDialog;

import java.util.Arrays;

public class ImportAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(ImportAction.class);
    private Project project;

    @Override public void actionPerformed(AnActionEvent e) {
        project = e.getData(DataKeys.PROJECT);
        PsiFile file = e.getData(DataKeys.PSI_FILE);

        try {
            MainDialog mainDialog = new MainDialog(project, file, false);
            mainDialog.setVisible(true);
        } catch (Throwable e1) {
            Messages.showErrorDialog(Stream.of(Arrays.asList(e1.getStackTrace()))
                                           .map(StackTraceElement::toString)
                                           .collect(com.annimon.stream.Collectors.joining("\n")), "title-exception");
        }

//        Messages.showDialog("showDialog", "title", new String[0], 0, null);

        //        List<String> source = new ArrayList<>();
        //
        //        source.add(project.getName());
        //        source.add(project.getBasePath());
        //        source.add(project.getProjectFilePath());
        //        source.add(file.getName());
        //        source.add(file.getFileType().getName());
        //        source.add(file.getVirtualFile().getPath());
        //
        //        StringBuilder builder = new StringBuilder();
        //        for (int i = 0; i < source.size(); i++) {
        //            builder.append("[").append(i).append("]").append(": ");
        //            builder.append(source.get(i)).append("\n");
        //        }
        //
        //        Messages.showMessageDialog(builder.toString(), "title", null);
        //
        //        try {
        //            Messages.showMessageDialog(dependencyAnalyzer.getDependLibraries().toString(), "gg", null);
        //        } catch (Exception e1) {
        //            Messages.showErrorDialog(Stream.of(Arrays.asList(e1.getStackTrace()))
        //                                           .map(o -> o.toString())
        //                                           .collect(com.annimon.stream.Collectors.joining("\n")), "GG");
        //            e1.printStackTrace();
        //        }
    }

    @Override public void update(AnActionEvent e) {
        super.update(e);
    }
}
