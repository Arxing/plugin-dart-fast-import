package org.arxing;

import org.arxing.ui.MainDialog;

public class Main {

    public static void main(String[] args) throws Exception {
        //        List<String> keywords = FileUtil.loadLines("W:\\plugins\\dart-fast-import\\src\\test_dependencies.txt");
        //        JFrame frame = new JFrame();
        //        frame.setSize(400, 100);
        //        AutoCompleteComboBox completeComboBox = new AutoCompleteComboBox(Stream.of(keywords).map(o -> {
        //            if (o.startsWith("dart")) {
        //                return LibTarget.ofDart(o.substring(5));
        //            } else if (o.startsWith("package:")) {
        //                String s = o.split(":", 2)[1];
        //                return LibTarget.ofPackage(s.split("/")[0], URI.create(s.split("/", 2)[1]));
        //            } else {
        //                return LibTarget.ofFile(URI.create(o));
        //            }
        //        }).toList());
        //        frame.add(completeComboBox);
        //        frame.setVisible(true);


        //        File
        //        LibInfo libInfo = new LibInfo("path", "file:///C:/Users/meisw/AppData/Roaming/Pub/Cache/hosted/pub.dartlang
        // .org/path-1.6.2/lib/",
        //                                      LibInfo.LibType.packages);
        //        List<String> children = libInfo.getAllTargets();
        //        System.out.println(children.stream().collect(Collectors.joining("\n")));
        //
        MainDialog mainDialog = new MainDialog(null, null, true);
        mainDialog.updateDependencies();
        mainDialog.setVisible(true);
        mainDialog.focusComboBox();


    }
}
