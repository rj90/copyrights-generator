package com.rafalj.copyrightsgenerator;

import com.rafalj.copyrightsgenerator.engine.ViewResolver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CopyrightsGeneratorApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ViewResolver viewResolver = new ViewResolver();
        FXMLLoader fxmlLoader = new FXMLLoader(viewResolver.getMainViewURL());
        Scene scene = new Scene(fxmlLoader.load(), 920, 520);
        stage.setTitle("Copyrights - Generator!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}