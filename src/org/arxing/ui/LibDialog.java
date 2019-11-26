package org.arxing.ui;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.Centerizer;

import org.arxing.DependencyAnalyzer;
import org.arxing.ImportsType;
import org.arxing.Printer;
import org.arxing.impl.DependencyAnalyzerImpl;
import org.arxing.impl.LibTarget;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class LibDialog extends JFrame {
    private Container container;
    private ButtonGroup importsOptionGroup;
    private LibComboBox comboBox;
    private LibDialogCallback callback;

    private ImportsType currentImportsType;
    private String currentPath;
    private DependencyAnalyzer dependencyAnalyzer;

    public LibDialog(Project project) throws Exception {
        super("Dart Fast Import");
        dependencyAnalyzer = project != null ? DependencyAnalyzer.getInstance(project) : new DependencyAnalyzerImpl(null);
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
        comboBox.updateModels(dependencyAnalyzer.getDependencies(null));
    }

    @Override public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
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

        comboBox = new LibComboBox();
        comboBox.setCallback(comboBoxCallback);
        comboBox.setPreferredSize(new Dimension(600, comboBox.getPreferredSize().height));
        Centerizer libPanel = new Centerizer(comboBox);

        JPanel completeGroup = new JPanel(new VerticalLayout(10));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("cancel");
        completeGroup.add(okButton);
        completeGroup.add(cancelButton);
        Centerizer completePanel = new Centerizer(completeGroup);

        container = new JPanel(new HorizontalLayout(15));
        ((JPanel) container).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        container.add(importsOptionPanel);
        container.add(libPanel);
        container.add(completePanel);
        setContentPane(container);


        okButton.addActionListener(okListener);
        cancelButton.addActionListener(cancelListener);
    }

    private ActionListener okListener = e -> {
        if (callback == null)
            return;
        callback.confirm(currentImportsType, currentPath);
    };

    private ActionListener cancelListener = e -> setVisible(false);

    private ActionListener importsOptionListener = e -> {
        currentImportsType = Stream.of(ImportsType.values())
                                   .filter(type -> type.getOption().equals(e.getActionCommand()))
                                   .findSingle()
                                   .orElseThrow();
    };

    private LibComboBox.LibComboBoxCallback comboBoxCallback = new LibComboBox.LibComboBoxCallback() {

        @Override public void onFindExtras(List<LibTarget> extras) {
            dependencyAnalyzer.putExtraDependencies(extras);
        }

        @Override public void onKeywordChanged(String keyword) {
            List<LibTarget> recommends = dependencyAnalyzer.getDependencies(keyword);
            Printer.print("關鍵字: %s", keyword);
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
