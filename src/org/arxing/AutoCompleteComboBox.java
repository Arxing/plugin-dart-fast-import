package org.arxing;

import com.annimon.stream.Stream;
import com.intellij.ui.CollectionComboBoxModel;

import org.arxing.impl.LibTarget;
import org.arxing.ui.ConfirmListener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AutoCompleteComboBox extends JComboBox<LibTarget> {
    public JTextField inputField = null;
    private List<LibTarget> source = new ArrayList<>();
    private List<LibTarget> show = new ArrayList<>();
    private String currentText;
    private String keyword;
    private int currentSelectedIndex = -1;
    private ConfirmListener listener;

    public AutoCompleteComboBox(final List<LibTarget> src) {
        super();
        setModel(new CollectionComboBoxModel<>());
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        updateSource(src);
        addItemListener(e -> {
            currentSelectedIndex = getSelectedIndex();
            updateUi();
            if (currentSelectedIndex != -1) {
                LibTarget target = getModel().getElementAt(currentSelectedIndex);
                currentText = target.toString();
                keyword = target.backTarget();
                filter();
                updateUi();
                currentSelectedIndex = -1;
            }
        });
    }

    public String getCurrentText() {
        return currentText;
    }

    public void setListener(ConfirmListener listener) {
        this.listener = listener;
    }

    public void updateSource(List<LibTarget> src) {
        currentText = "";
        keyword = "";
        currentSelectedIndex = -1;
        source.clear();
        source.addAll(src);
        filter();
        setSelectedItem(null);
    }

    private void filter() {
        show.clear();
        List<LibTarget> tmp;
        if (keyword == null || keyword.isEmpty()) {
            tmp = Stream.of(source).sorted(Comparator.comparing(LibTarget::toString)).toList();
        } else {
            tmp = Stream.of(source)
                        .filter(el -> el.toString().contains(keyword))
                        .sorted(Comparator.comparing(LibTarget::toString))
                        .toList();
        }
        show.addAll(tmp);
        CollectionComboBoxModel<LibTarget> model = (CollectionComboBoxModel<LibTarget>) getModel();
        model.removeAll();
        model.add(show);
    }

    public void updateUi() {
        inputField.setText(currentText);
        showPopup();
    }

    public void notifyOk() {
        if (listener != null) {
            try {
                listener.onConfirm(null, currentText);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println(String.format("確認: %s", currentText));
    }

    @Override public void setEditor(ComboBoxEditor editor) {
        super.setEditor(editor);
        if (editor.getEditorComponent() instanceof JTextField) {
            inputField = (JTextField) editor.getEditorComponent();
            inputField.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                        currentSelectedIndex = getSelectedIndex();
                        updateUi();
                    } else if (key == KeyEvent.VK_ENTER) {
                        if (currentSelectedIndex != -1) {
                            LibTarget target = getModel().getElementAt(currentSelectedIndex);
                            currentText = target.toString();
                            keyword = target.backTarget();
                            filter();
                            updateUi();
                            currentSelectedIndex = -1;
                        } else {
                            notifyOk();
                        }
                    } else {
                        currentText = inputField.getText();
                        keyword = currentText;
                        filter();
                        updateUi();
                    }
                }
            });
        }
    }
}