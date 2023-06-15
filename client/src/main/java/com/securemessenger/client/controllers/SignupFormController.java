package com.securemessenger.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemessenger.client.Application;
import com.securemessenger.client.account.LocalData;
import com.securemessenger.client.models.network.RMessage;
import com.securemessenger.client.models.network.RType;
import com.securemessenger.client.models.network.structs.Registration;
import com.securemessenger.client.network.Host;
import com.securemessenger.client.network.WebSocketClient;
import com.securemessenger.client.signal.SignalKeys;
import com.securemessenger.client.signal.SignalUtility;
import com.securemessenger.client.utility.FormLoader;
import com.securemessenger.client.utility.Utility;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.IOException;

public class SignupFormController {

    @FXML
    private Button backBtn;

    @FXML
    private Label errLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField pwdRetField;

    @FXML
    private Button signupBtn;

    @FXML
    private TextField usernameField;

    private WebSocketClient webSocketClient;
    private LocalData localData;

    private ObjectMapper objectMapper;

    private SignalKeys signalKeys;
    private PreKeyBundle keyBundle;

    public SignupFormController(LocalData localData) {
        webSocketClient = new WebSocketClient();
        objectMapper = new ObjectMapper();

        this.localData = localData;

        webSocketClient.setListener(new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    RMessage rMessage = objectMapper.readValue(text, RMessage.class);
                    Object content = rMessage.getContent();
                    switch (rMessage.getType()) {
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

    private void error(Object content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText((String) content);

        alert.showAndWait();
    }

    private void serverMessage(Object content) {
        String message = (String) content;

        if(message.equals("REG_SUCCESS")) {
            SignalProtocolStore signalProtocolStore = new InMemorySignalProtocolStore(signalKeys.getIdentityKeyPair(),
                    keyBundle.getRegistrationId());
            signalProtocolStore.storePreKey(keyBundle.getPreKeyId(), signalKeys.getPreKeyRecord());
            signalProtocolStore.storeSignedPreKey(keyBundle.getSignedPreKeyId(), signalKeys.getSignedPreKeyRecord());

            try {
                SignalUtility.saveProtocolStore(signalProtocolStore, keyBundle.getPreKeyId(), keyBundle.getSignedPreKeyId());
            } catch (InvalidKeyIdException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {
                try {
                    loadLoginForm();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }



    @FXML
    void onBackBtnCick(ActionEvent event) throws IOException {
        loadLoginForm();
    }

    @FXML
    void onMouseEntered(MouseEvent event) {

    }

    @FXML
    void onMouseExited(MouseEvent event) {

    }

    @FXML
    void onSignupBtnCick(ActionEvent event) throws JsonProcessingException, InvalidKeyException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String rtpPassword = pwdRetField.getText();

        signalKeys = new SignalKeys();
        keyBundle = signalKeys.getKeyBundle(KeyHelper.generateRegistrationId(false),
                Utility.generateDeviceId());

        if(password.equals(rtpPassword) && password.length() > 5 && username.length() > 4) {
            webSocketClient.sendMessage(objectMapper.writeValueAsString(new RMessage(RType.REGISTRATION,
                    new Registration(username, password, SignalUtility.serializeKeyBundle(keyBundle)))));
        }
    }

    private void loadLoginForm() throws IOException {
        FormLoader.loadForm("login-form.fxml", new LoginFormController(localData), errLabel.getScene());
    }

}

