package org.arxing.ui;

import com.intellij.ui.CollectionComboBoxModel;

import org.arxing.Printer;
import org.arxing.impl.LibTarget;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class LibComboBox extends JComboBox<LibTarget> implements ItemListener {
    private CollectionComboBoxModel<LibTarget> model = new CollectionComboBoxModel<>();
    private JTextField textField;
    private boolean inputEnable = false;
    private boolean keying = false;
    private String currentText;
    private int currentIndex;
    private LibComboBoxCallback callback;
    private int acceptCounter;

    public LibComboBox() {
        setModel(model);
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        addItemListener(this);
        resetState();
    }

    private void resetState() {
        acceptCounter = 0;
        currentIndex = -1;
        currentText = "";
        inputEnable = true;
    }

    private void syncText() {
        textField.setText(currentText);
        Printer.print("同步文字: \"%s\"", currentText);
    }

    public void updateModels(List<LibTarget> models) {
        model.removeAll();
        model.add(models);
        currentIndex = -1;
    }

    public void setCallback(LibComboBoxCallback callback) {
        this.callback = callback;
    }

    private void selectCurrent() {
        Printer.print("selectCurrent(%d)", currentIndex);
        if (currentIndex == -1)
            return;
        LibTarget target = model.getElementAt(currentIndex);
        currentText = target.toString();
        Printer.print("currentIndex=%d, currentText=%s", currentIndex, currentText);
        callback.onTargetSelected(target);
        showPopup();
    }

    private void acceptSelected() {
        callback.onConfirm(currentText);
    }

    @Override public void setEditor(ComboBoxEditor anEditor) {
        super.setEditor(anEditor);
        if (!(editor.getEditorComponent() instanceof JTextField))
            return;
        textField = (JTextField) editor.getEditorComponent();
        textField.addKeyListener(keyAdapter);
    }

    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override public void keyPressed(KeyEvent e) {
            inputEnable = false;
            keying = true;
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    break;
                case KeyEvent.VK_ENTER:
                    Printer.print("按下enter");
                    selectCurrent();
                    break;
                case KeyEvent.VK_ESCAPE:
                    callback.onEsc();
                    break;
                default:
                    inputEnable = true;
                    break;
            }
        }

        @Override public void keyReleased(KeyEvent e) {
            keying = false;
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    if (currentIndex == -1)
                        acceptSelected();
                    break;
                default:
                    if (inputEnable) {
                        Printer.print("輸入了: \"%s\"", String.valueOf(e.getKeyChar()));
                        currentText = textField.getText();
                        callback.onKeywordChanged(currentText);
                    }
                    syncText();
                    break;
            }
            showPopup();
        }
    };

    @Override public void itemStateChanged(ItemEvent e) {
        int selectedIndex = model.getElementIndex((LibTarget) e.getItem());
        if (currentIndex == getSelectedIndex() || selectedIndex == -1)
            return;
        currentIndex = selectedIndex;
        setSelectedIndex(currentIndex);
        Printer.print("itemStateChanged: index=%d", selectedIndex);
        if (!keying) {
            Printer.print("用滑鼠選擇");
            selectCurrent();
            currentText = textField.getText();
            callback.onKeywordChanged(currentText);
        }
    }

    public interface LibComboBoxCallback {

        void onTargetSelected(LibTarget target);

        void onKeywordChanged(String keyword);

        void onEsc();

        void onConfirm(String target);
    }
}
