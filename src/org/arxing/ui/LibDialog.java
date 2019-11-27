package org.arxing.ui;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.Centerizer;

import org.arxing.DependencyAnalyzer;
import org.arxing.ImportsType;
import org.arxing.Printer;
import org.arxing.Settings;
import org.arxing.impl.DependencyAnalyzerImpl;
import org.arxing.impl.LibTarget;
import org.arxing.impl.LibType;
import org.arxing.impl.SettingsImpl;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class LibDialog extends JFrame {
    private Container container;
    private ButtonGroup importsOptionGroup;
    private LibComboBox comboBox;
    private LibDialogCallback callback;
    private JCheckBox cbRecursive;

    private ImportsType currentImportsType;
    private String currentPath;
    private DependencyAnalyzer dependencyAnalyzer;
    private Settings settings;

    public LibDialog(Project project) throws Exception {
        super("Dart Fast Import");
        dependencyAnalyzer = project != null ? DependencyAnalyzer.getInstance(project) : new DependencyAnalyzerImpl(null);
        settings = project != null ? Settings.getInstance(project) : new SettingsImpl(null);
        resetState();
        initUI();
        setResizable(false);
        pack();
        updateDependencies();
    }

    public void setCallback(LibDialogCallback callback) {
        this.callback = callback;
    }

    private void resetState() {
        currentImportsType = ImportsType.IMPORT;
    }

    public void updateDependencies() throws Exception {
        dependencyAnalyzer.updateDependencies();
        comboBox.updateModels(dependencyAnalyzer.getDependencies(null, null));
    }

    @Override public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
            comboBox.requestFocus();
        }
    }

    private void initUI() {
        // 匯入選項群組
        JPanel importsOptionPanel = new JPanel(new VerticalLayout());
        importsOptionGroup = new ButtonGroup();
        for (int i = 0; i < ImportsType.values().length; i++) {
            JRadioButton radioButton = new JRadioButton(ImportsType.values()[i].getOption());
            radioButton.setSelected(i == 0);
            radioButton.setMnemonic(i);
            radioButton.addActionListener(importsOptionListener);
            importsOptionGroup.add(radioButton);
            importsOptionPanel.add(radioButton);
        }

        comboBox = new LibComboBox(settings.isRecursive());
        comboBox.setCallback(comboBoxCallback);
        comboBox.setPreferredSize(new Dimension(600, comboBox.getPreferredSize().height));
        Centerizer libPanel = new Centerizer(comboBox);

        JPanel completeGroup = new JPanel(new VerticalLayout(10));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("cancel");
        cbRecursive = new JCheckBox("Recursive Mode", settings.isRecursive());
        completeGroup.add(okButton);
        completeGroup.add(cancelButton);
        completeGroup.add(cbRecursive);
        Centerizer completePanel = new Centerizer(completeGroup);

        container = new JPanel(new HorizontalLayout(15));
        ((JPanel) container).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        container.add(importsOptionPanel);
        container.add(libPanel);
        container.add(completePanel);
        setContentPane(container);


        okButton.addActionListener(okListener);
        cancelButton.addActionListener(cancelListener);
        cbRecursive.addItemListener(recursiveListener);
    }

    private ActionListener okListener = e -> callback.confirm(currentImportsType, currentPath);

    private ItemListener recursiveListener = e -> settings.setRecursive(cbRecursive.isSelected());

    private ActionListener cancelListener = e -> setVisible(false);

    private ActionListener importsOptionListener = e -> {
        currentImportsType = Stream.of(ImportsType.values())
                                   .filter(type -> type.getOption().equals(e.getActionCommand()))
                                   .findSingle()
                                   .orElseThrow();
        switch (currentImportsType) {
            case PART:
            case PART_OF:
                comboBox.updateModels(dependencyAnalyzer.getDependencies(Collections.singletonList(LibType.file), null));
                break;
        }
    };

    private LibComboBox.LibComboBoxCallback comboBoxCallback = new LibComboBox.LibComboBoxCallback() {

        @Override public void onTextChanged(String text) {
            currentPath = text;
        }

        @Override public void onFindExtras(List<LibTarget> extras) {
            dependencyAnalyzer.putExtraDependencies(extras);
        }

        @Override public void onKeywordChanged(String keyword) {
            List<LibTarget> recommends = dependencyAnalyzer.getDependencies(null, keyword);
            comboBox.updateModels(recommends);
        }

        @Override public void onEsc() {
            setVisible(false);
        }

        @Override public void onConfirm(String target) {
            callback.confirm(currentImportsType, target);
        }
    };

    public interface LibDialogCallback {

        void confirm(ImportsType importsType, String path);
    }
}
