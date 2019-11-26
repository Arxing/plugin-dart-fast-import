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
import java.util.Objects;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.ComboPopup;

public class AutoCompleteComboBox extends JComboBox<LibTarget> {
    public JTextField inputField = null;
    private List<LibTarget> source = new ArrayList<>();
    private List<LibTarget> show = new ArrayList<>();
    private String currentText;
    private String keyword;
    private int currentSelectedIndex = -1;
    private ConfirmListener listener;
    private boolean moving = false;
    private boolean confirming = false;

    private static void print(String format, Object... params) {
        System.out.println(String.format(format, params));
    }

    public AutoCompleteComboBox(final List<LibTarget> src) {
        super();
        setModel(new CollectionComboBoxModel<>());
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        updateSource(src);

        getPopupList().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            if (moving) {
                if (currentSelectedIndex == getSelectedIndex())
                    return;
                currentSelectedIndex = getSelectedIndex();
                updateUi();
                print("物件選取 cur=%d", currentSelectedIndex);
            }
        });
    }

    private JList getPopupList() {
        ComboPopup popup = (ComboPopup) getUI().getAccessibleChild(this, 0);
        return popup.getList();
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
                @Override public void keyPressed(KeyEvent e) {
                    int key = e.getKeyCode();
                    moving = false;
                    confirming = false;
                    switch (key) {
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                            moving = true;
                            print("* 上下移動中");
                            break;
                        case KeyEvent.VK_ENTER:
                            print("* 按下確認, idx=%d", currentSelectedIndex);
                            confirming = true;
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
                            break;
                    }
                }

                @Override public void keyReleased(KeyEvent e) {
                    // 移動中的話略過此事件
                    if (moving || confirming)
                        return;
                    String s = String.valueOf(e.getKeyChar());
                    print("keyReleased: %s", s);
                    currentText = inputField.getText();
                    keyword = currentText;
                    filter();
                    updateUi();
                }
            });
        }
    }
}