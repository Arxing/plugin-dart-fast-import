package org.arxing;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.CollectionListModel;

import org.arxing.impl.LibTarget;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AutoCompleteComboBox extends JComboBox<LibTarget> {
    public int caretPos = 0;
    public JTextField inputField = null;
    private List<LibTarget> source = new ArrayList<>();
    private List<LibTarget> show = new ArrayList<>();
    private String currentInput;

    public AutoCompleteComboBox(final List<LibTarget> source) {
        super();
        this.source.addAll(source);
        filterAndUpdate(null, false);
        setEditor(new BasicComboBoxEditor());
        setEditable(true);
        setSelectedItem(null);
        addlistener
    }

    public void setSelectedIndex(int index) {
        super.setSelectedIndex(index);
        inputField.setText(getItemAt(index).toString());
        inputField.setSelectionEnd(caretPos + inputField.getText().length());
        inputField.moveCaretPosition(caretPos);
    }

    private void filterAndUpdate(String keyword, boolean updateUi) {
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
        System.out.println(String.format("更新! key=%s", keyword));
        //        System.out.println(Stream.of(show).collect(Collectors.joining("\n")));
        setModel(new CollectionComboBoxModel<>(show));

        if (updateUi) {
            inputField.setText(currentInput);
            showPopup();
        }
    }

    private void computePreviousTarget(String target) {
        if (target.contains(":")) {
            String[] splits = target.split(":", 2);

        } else {

        }
    }

    public void setEditor(ComboBoxEditor editor) {
        super.setEditor(editor);
        if (editor.getEditorComponent() instanceof JTextField) {
            inputField = (JTextField) editor.getEditorComponent();
            inputField.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    System.out.println("pressed");
                    currentInput = inputField.getText();
                    filterAndUpdate(currentInput, true);
                }
            });


            //            inputField.addKeyListener(new KeyAdapter() {
            //
            //                @Override public void keyReleased(KeyEvent ev) {
            //                    char key = ev.getKeyChar();
            //                    System.out.println("keyReleased: " + String.valueOf(key));
            //                    if (!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key)))
            //                        return;
            //
            //                    caretPos = inputField.getCaretPosition();
            //                    String text = "";
            //                    try {
            //                        text = inputField.getText(0, caretPos);
            //                    } catch (javax.swing.text.BadLocationException e) {
            //                        e.printStackTrace();
            //                    }
            //
            //                    for (int i = 0; i < getItemCount(); i++) {
            //                        String element = getItemAt(i);
            //                        if (element.startsWith(text)) {
            //                            setSelectedIndex(i);
            //                            return;
            //                        }
            //                    }
            //                }
            //            });
        }
    }
}