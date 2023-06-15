package com.securemessenger.client;

import com.securemessenger.client.account.LocalData;
import com.securemessenger.client.controllers.MainFormController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-form.fxml"));
        fxmlLoader.setController(new MainFormController(new LocalData(new File("local.data"))));

        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        stage.setTitle("Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws InterruptedException {
        launch();
    }
}