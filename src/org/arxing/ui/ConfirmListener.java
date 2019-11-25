package org.arxing.ui;

import java.io.IOException;

public interface ConfirmListener {

    void onConfirm(String kind, String target) throws IOException;
}
