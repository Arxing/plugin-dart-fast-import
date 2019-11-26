package org.arxing.ui;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ComboBoxCompositeEditor;
import com.intellij.ui.ComboboxEditorTextField;

import org.arxing.Printer;
import org.arxing.impl.LibTarget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class LibComboBox extends JComboBox<LibTarget> {
    private CollectionComboBoxModel<LibTarget> model = new CollectionComboBoxModel<>();
    private JTextField textField;
    private boolean selectFromMouse;
    private String currentText;
    private LibComboBoxCallback callback;
    private int tmpIndex;

    public LibComboBox() {
        setModel(model);
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        resetState();
    }

    private void resetState() {
        currentText = "";
        selectFromMouse = true;
    }

    private void syncText() {
        textField.setText(currentText);
//        Printer.print("同步文字: \"%s\"", currentText);
    }

    public void updateModels(List<LibTarget> models) {
        model.removeAll();
        model.add(models);
        model.sort(LibTarget::compareTo);
        //        Printer.print("==============================================================");
        //        Printer.print(Stream.of(models).map(LibTarget::toString).collect(Collectors.joining("\n")));
    }

    public void setCallback(LibComboBoxCallback callback) {
        this.callback = callback;
    }

    //    private void selectCurrent() {
    //        Printer.print("selectCurrent(%d)", getSelectedIndex());
    //        if (getSelectedIndex()==-1)
    //            return;
    //        LibTarget target = model.getElementAt(getSelectedIndex());
    //        currentText = target.toString();
    //        Printer.print("currentIndex=%d, currentText=%s", getSelectedIndex(), currentText);
    //        callback.onTargetSelected(target);
    //        showPopup();
    //    }

    private void acceptSelected() {
        callback.onConfirm(currentText);
    }

    @Override public void showPopup() {
        if (isPopupVisible())
            hidePopup();
        super.showPopup();
    }

    @Override public void setEditor(ComboBoxEditor anEditor) {
        super.setEditor(anEditor);
        if (!(editor.getEditorComponent() instanceof JTextField))
            return;
        textField = (JTextField) editor.getEditorComponent();
        textField.addKeyListener(keyAdapter);
    }

    private KeyAdapter keyAdapter = new KeyAdapter() {
        boolean needShowPopup;
        boolean inputEnable;

        @Override public void keyPressed(KeyEvent e) {
            inputEnable = false;
            selectFromMouse = false;
            needShowPopup = true;
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    break;
                case KeyEvent.VK_ENTER:
//                    Printer.print("按下enter, tmpIndex=%d", tmpIndex);
                    if (tmpIndex != -1) {
//                        Printer.print("設置為%d", tmpIndex);
                        currentText = model.getElementAt(tmpIndex).toString();
                        setSelectedIndex(tmpIndex);
                        LibTarget target = model.getElementAt(tmpIndex);
                        List<LibTarget> extras = target.getRelationTargets();
                        callback.onFindExtras(extras);
                        updateModels(extras);
                        needShowPopup = !target.isLeaf();
                        tmpIndex = -1;
                    }
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
            selectFromMouse = true;
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    if (needShowPopup) {
                        showPopup();
                    }
                    break;
                default:
                    if (inputEnable) {
//                        Printer.print("輸入了: \"%s\"", String.valueOf(e.getKeyChar()));
                        currentText = textField.getText();
                        callback.onKeywordChanged(currentText);
                        syncText();
                        if (needShowPopup) {
                            showPopup();
                        }
                    }
                    break;
            }
        }
    };

    @Override public void setSelectedIndex(int anIndex) {
        if (anIndex == -1)
            return;
        tmpIndex = anIndex;
        super.setSelectedIndex(tmpIndex);
//        Printer.print("setSelectedIndex: %d", tmpIndex);
        if (selectFromMouse) {
            currentText = model.getElementAt(tmpIndex).toString();
        }
        syncText();
    }

    public interface LibComboBoxCallback {

        void onFindExtras(List<LibTarget> extras);

        void onKeywordChanged(String keyword);

        void onEsc();

        void onConfirm(String target);
    }
}
