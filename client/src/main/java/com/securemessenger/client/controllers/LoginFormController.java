package com.securemessenger.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemessenger.client.account.LocalData;
import com.securemessenger.client.models.network.RMessage;
import com.securemessenger.client.models.network.RType;
import com.securemessenger.client.models.network.structs.Authentication;
import com.securemessenger.client.models.network.structs.Authorization;
import com.securemessenger.client.network.Host;
import com.securemessenger.client.network.WebSocketClient;
import com.securemessenger.client.utility.FormLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.io.IOException;

public class LoginFormController {

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginBtn;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label signUpBtn;

    private LocalData localData;
    private WebSocketClient webSocketClient;
    private ObjectMapper objectMapper;

    public LoginFormController(LocalData localData) {
        this.localData = localData;
        this.objectMapper = new ObjectMapper();

        webSocketClient = new WebSocketClient();
        webSocketClient.setListener(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    RMessage rMessage = objectMapper.readValue(text, RMessage.class);
                    Object content = rMessage.getContent();
                    switch (rMessage.getType()) {
                        case SESSION_KEY -> session(content);
                        case ERROR -> error(content);
                        case SERVER_MESSAGE -> serverMessage(content);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        webSocketClient.connect(Host.URL);
    }

    private void serverMessage(Object content) {
        if(content.toString().contains("AUTH_SUCCESS")) {
            localData.setUsername(loginField.getText());
            Platform.runLater(() -> {
                try {
                    login();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void session(Object content) {
        localData.setSession((String) content);
        send(new RMessage(RType.AUTHORIZATION, new Authorization((String) content)));
    }

    private void error(Object content) {
        errorLabel.setVisible(true);
        Platform.runLater(() -> {
            errorLabel.setText((String) content);
        });
    }

    @FXML
    void onLoginBtnClick(ActionEvent event) throws IOException {
        String username = loginField.getText();
        String password = passwordField.getText();
        send(new RMessage(RType.AUTHENTICATION, new Authentication(username, password)));
    }

    @FXML
    void onMouseEntered(MouseEvent event) {

    }

    @FXML
    void onMouseExited(MouseEvent event) {

    }

    @FXML
    void onSignupBtnClick(MouseEvent event) throws IOException {
        webSocketClient.close();
        FormLoader.loadForm("signup-form.fxml", new SignupFormController(localData), signUpBtn.getScene());
    }

    private void send(RMessage rMessage) {
        try {
            webSocketClient.sendMessage(objectMapper.writeValueAsString(rMessage));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void login() throws IOException {
        FormLoader.loadForm("main-form.fxml", new MainFormController(localData), loginBtn.getScene());
    }

}

