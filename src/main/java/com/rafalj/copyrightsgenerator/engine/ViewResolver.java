package com.rafalj.copyrightsgenerator.engine;

import com.rafalj.copyrightsgenerator.CopyrightsGeneratorApp;

import java.net.URL;

public class ViewResolver {
    public URL getMainViewURL() {
        return CopyrightsGeneratorApp.class.getResource("fxml/MainView.fxml");
    }
}
