package org.arxing;

import org.arxing.ui.LibDialog;

import javax.swing.WindowConstants;

public class Main {

    public static void main(String[] args) throws Exception {
        LibDialog libDialog = new LibDialog(null);
        libDialog.setCallback((importsType, path) -> {
            Printer.print("select: %s %s", importsType.getOption(), path);
        });
        libDialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        libDialog.setVisible(true);
    }
}
