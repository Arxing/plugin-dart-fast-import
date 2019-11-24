package org.arxing.ui;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import org.arxing.DependencyAnalyzer;
import org.arxing.AutoCompleteComboBox;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import java.awt.Container;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainDialog extends JFrame {
    private Project project;
    private PsiFile file;
    private Container container;
    private DependencyAnalyzer dependencyAnalyzer;
    private JComboBox<String> comboBox;

    public MainDialog(Project project, PsiFile file, boolean debug) throws Exception {
        this.project = project;
        this.file = file;
        this.dependencyAnalyzer = DependencyAnalyzer.getInstance(project);
        setSize(800, 200);
        container = createUi();
        setContentPane(container);
        if (debug) {

            comboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                    "apple",
                    ""
            }));
        } else
            updateDependencies();
        setTitle("FastImport");
        setAlwaysOnTop(true);
    }

    public void updateDependencies() throws Exception {
        dependencyAnalyzer.updateDependencies();
        comboBox.setModel(new ListComboBoxModel<String>(dependencyAnalyzer.filterDependencies(null)));
    }

    private Container createUi() {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);

//        comboBox = new AutoCompleteComboBox(new ArrayList<>());
        panel.add(comboBox);
        return panel;
    }
}
