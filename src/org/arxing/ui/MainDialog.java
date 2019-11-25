package org.arxing.ui;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;

import org.arxing.AutoCompleteComboBox;
import org.arxing.DependencyAnalyzer;
import org.arxing.impl.LibTarget;
import org.jdesktop.swingx.VerticalLayout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class MainDialog extends JFrame {
    private static String[] options = {"import", "export", "part", "part of"};
    private Project project;
    private VirtualFile file;
    private Container container;
    private DependencyAnalyzer dependencyAnalyzer;
    private AutoCompleteComboBox comboBox;
    private boolean debug;
    private ButtonGroup group;

    public MainDialog(Project project, VirtualFile file, boolean debug) throws Exception {
        this.project = project;
        this.file = file;
        this.debug = debug;
        this.dependencyAnalyzer = debug ? null : DependencyAnalyzer.getInstance(project);
        container = createUi();
        setContentPane(container);
        updateDependencies();
        setTitle("FastImport");
        setAlwaysOnTop(true);
        pack();
    }

    public void setListener(ConfirmListener listener) {
        comboBox.setListener((kind, target) -> {
            int index = group.getSelection().getMnemonic();
            listener.onConfirm(options[index], target);
        });
    }

    public void updateDependencies() throws Exception {
        if (debug) {
            comboBox.updateSource(Arrays.asList(LibTarget.ofDart("io"), LibTarget.ofDart("convert")));
        } else {
            dependencyAnalyzer.updateDependencies();
            List<LibTarget> targets = dependencyAnalyzer.getDependencies();
            comboBox.updateSource(targets);
        }
    }

    public void focusComboBox() {
        comboBox.requestFocus();
    }

    private Container createUi() {
        JPanel radioGroup = new JPanel(new VerticalLayout());

        group = new ButtonGroup();
        AtomicInteger i = new AtomicInteger();
        Stream.of(options).map(JRadioButton::new).forEach(b -> {
            b.setMnemonic(i.getAndIncrement());
            group.add(b);
            radioGroup.add(b);
        });
        ((JRadioButton) radioGroup.getComponent(0)).setSelected(true);

        JPanel panel = new JPanel();
        JTextField textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);

        comboBox = new AutoCompleteComboBox(new ArrayList<>());
        comboBox.setPreferredSize(new Dimension(600, comboBox.getPreferredSize().height));

        JLabel message = new JLabel("idle");
        JButton update = new JButton("update dependencies");
        update.addActionListener(e -> {
            message.setText("updating...");
            try {
                updateDependencies();
            } catch (Exception e1) {
            }
            message.setText("success");
        });

        JPanel rightPanel = new JPanel(new VerticalLayout());
        rightPanel.add(update);
        rightPanel.add(message);

        JPanel middlePanel = new JPanel(new VerticalLayout());
        JButton ok = new JButton("OK");
        ok.setPreferredSize(new Dimension(50,30));
        middlePanel.add(comboBox);
        middlePanel.add(ok, null);
        ok.addActionListener(e -> comboBox.notifyOk());


        panel.add(radioGroup);
        panel.add(middlePanel);
        panel.add(rightPanel);
        return panel;
    }

}
