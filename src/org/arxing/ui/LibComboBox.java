package org.arxing.ui;

import com.annimon.stream.Stream;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.PathUtilRt;

import org.arxing.impl.LibTarget;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    private String ENABLE_INPUT_REGEX = "[a-zA-Z0-9:/.'_]*";
    private boolean needReshowPopup;
    private boolean recursive;
    private boolean isWindows;

    public LibComboBox(boolean recursive) {
        this.recursive = recursive;
        isWindows = PathUtilRt.Platform.CURRENT == PathUtilRt.Platform.WINDOWS;
        setModel(model);
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        resetState();
    }

    private void resetState() {
        currentText = "";
        needReshowPopup = true;
        selectFromMouse = true;
    }

    private void syncText() {
        textField.setText(currentText);
        callback.onTextChanged(currentText);
    }

    public void updateModels(List<LibTarget> newModels) {
        newModels = Stream.of(newModels).filterNot(target -> target.toString().isEmpty()).distinctBy(LibTarget::toString).toList();
        needReshowPopup = model.getSize() != newModels.size();
        model.removeAll();
        model.add(newModels);
        model.sort(LibTarget::compareTo);
    }

    public void setCallback(LibComboBoxCallback callback) {
        this.callback = callback;
    }

    private void acceptSelected() {
        callback.onConfirm(currentText);
    }

    @Override public void showPopup() {
        if (isPopupVisible()) {
            if (needReshowPopup) {
                hidePopup();
                super.showPopup();
            }
        } else {
            super.showPopup();
        }
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
                    if (!isWindows) {
                        if (tmpIndex > 0)
                            setSelectedIndex(tmpIndex - 1);
                        else
                            setSelectedIndex(0);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (!isWindows) {
                        if (tmpIndex < model.getSize() - 1)
                            setSelectedIndex(tmpIndex + 1);
                        else
                            setSelectedIndex(model.getSize() - 1);
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    needShowPopup = false;
                    if (tmpIndex != -1) {
                        LibTarget target = model.getElementAt(tmpIndex);
                        currentText = target.toString();
                        setSelectedIndex(tmpIndex);
                        if (!recursive) {
                            List<LibTarget> extras = target.getRelationTargets();
                            callback.onFindExtras(extras);
                            updateModels(extras);
                        }
                        needShowPopup = !target.isLeaf();
                        tmpIndex = -1;
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    callback.onEsc();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    inputEnable = true;
                    break;
                default:
                    inputEnable = String.valueOf(e.getKeyChar()).matches(ENABLE_INPUT_REGEX);
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
                    } else {
                        acceptSelected();
                    }
                    break;
                default:
                    if (inputEnable) {
                        currentText = textField.getText();
                        callback.onKeywordChanged(currentText);
                        if (needShowPopup) {
                            showPopup();
                        }
                    }
                    syncText();
                    break;
            }
        }
    };

    @Override protected void selectedItemChanged() {
        super.selectedItemChanged();
        if (getComponentPopupMenu() != null)
            showPopup();
    }

    @Override public void setSelectedIndex(int anIndex) {
        if (anIndex == -1)
            return;
        tmpIndex = anIndex;
        super.setSelectedIndex(tmpIndex);
        if (selectFromMouse) {
            LibTarget target = model.getElementAt(tmpIndex);
            currentText = target.toString();
            if (!recursive) {
                List<LibTarget> extras = target.getRelationTargets();
                callback.onFindExtras(extras);
                updateModels(extras);
            }
            tmpIndex = -1;

            if (target.isLeaf())
                acceptSelected();
            else
                showPopup();
        }
        syncText();
    }

    public interface LibComboBoxCallback {

        void onTextChanged(String text);

        void onFindExtras(List<LibTarget> extras);

        void onKeywordChanged(String keyword);

        void onEsc();

        void onConfirm(String target);
    }
}
