module com.rafalj.copyrightsgenerator {
    requires com.opencsv;
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires org.eclipse.jgit;


    opens com.rafalj.copyrightsgenerator to javafx.fxml;
    exports com.rafalj.copyrightsgenerator;
    exports com.rafalj.copyrightsgenerator.controller;
    exports com.rafalj.copyrightsgenerator.model;
    opens com.rafalj.copyrightsgenerator.controller to javafx.fxml;
}