package com.securemessenger.client.utility;

import com.securemessenger.client.Application;
import com.securemessenger.client.controllers.SignupFormController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class FormLoader {
    public static void loadForm(String fxml, Object controller, Scene currentScene) throws IOException {
        Stage currentStage = (Stage) currentScene.getWindow();

        FXMLLoader loader = new FXMLLoader(Application.class.getResource(fxml));
        loader.setController(controller);

        Parent root = loader.load();
        Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());

        Stage newStage = new Stage();
        newStage.setScene(newScene);

        currentStage.close();

        newStage.initModality(Modality.APPLICATION_MODAL);
        //newStage.showAndWait();

        newStage.show();
    }
}
