package com.securemessenger.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemessenger.client.Application;
import com.securemessenger.client.account.LocalData;
import com.securemessenger.client.models.network.RMessage;
import com.securemessenger.client.models.network.RType;
import com.securemessenger.client.models.network.structs.*;
import com.securemessenger.client.network.Host;
import com.securemessenger.client.network.WebSocketClient;
import com.securemessenger.client.signal.SignalSession;
import com.securemessenger.client.signal.SignalUtility;
import com.securemessenger.client.utility.FormLoader;
import com.securemessenger.client.utility.Utility;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignalProtocolStore;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainFormController implements Initializable {

    @FXML
    private AnchorPane contactsPane;

    @FXML
    private TextField inputField;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView settingsBtn;

    @FXML
    private ListView<Msg> dialogPane;

    private WebSocketListener webSocketListener;
    private WebSocketClient webSocketClient;

    private LocalData localData;
    private ObjectMapper objectMapper;
    private boolean authorized;

    private String accountUsername;

    private String currentSelectedUser;
    private Contacts contacts;
    private SignalProtocolStore signalProtocolStore;

    public MainFormController(LocalData localData) {
        this.localData = localData;

        webSocketClient = new WebSocketClient();
        objectMapper = new ObjectMapper();

        webSocketListener = new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    RMessage rMessage = objectMapper.readValue(text, RMessage.class);
                    Object content = rMessage.getContent();
                    switch (rMessage.getType()) {
                        case SERVER_MESSAGE -> serverMessage(content);
                        case CONTACTS -> contacts(content);
                        case ERROR -> error(content);
                        case MESSAGE -> receiveChatMessage(content);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InvalidMessageException e) {
                    throw new RuntimeException(e);
                } catch (InvalidVersionException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        webSocketClient.setListener(webSocketListener);
        webSocketClient.connect(Host.URL);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            try {
                if(localData.getSession() == null) {
                    webSocketClient.close();
                    loadLoginForm();
                } else {
                    send(new RMessage(RType.AUTHORIZATION, new Authorization(localData.getSession())));
                    signalProtocolStore = SignalUtility.loadLocalProtocolStore();
                    accountUsername = localData.getUsername();
                }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        dialogPane.setStyle("-fx-control-inner-background: #16191f; -fx-control-inner-background-alt: derive(-fx-control-inner-background, 0%);");
        dialogPane.setCellFactory(new Callback<ListView<Msg>, ListCell<Msg>>() {
            @Override
            public ListCell<Msg> call(ListView<Msg> listView) {
                return new RoundedMessageCell();
            }
        });
    }

    @FXML
    void onActionSearch(ActionEvent event) {
        send(new RMessage(RType.ADD_CONTACT, new Username(searchField.getText())));
        updateContacts();
    }

    @FXML
    void onInputField(ActionEvent event) throws Exception {
        sendChatMessage(currentSelectedUser, inputField.getText());
        inputField.clear();
    }

    @FXML
    void onSettingsBtnClick(MouseEvent event) {
        send(new RMessage(RType.GET_MESSAGES, new Username("tuser")));
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
        if(message.equals("AUTH_SUCCESS"))  {
            authorized = true;
            updateContacts();
        }

        if(message.equals("SESSION_NOT_FOUND")) {
            Platform.runLater(() -> {
                try {
                    loadLoginForm();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void contacts(Object content) {
        Platform.runLater(() -> {
            contactsPane.getChildren().clear();
            Contacts contacts = objectMapper.convertValue(content, Contacts.class);
            this.contacts = contacts;
            UserData[] contactsList = contacts.getContacts();
            for(int i = 0; i < contactsList.length; i++) {
                SignalUtility.setContactKeyBundle(contactsList[i].getUsername(), contactsList[i].getPublicKey());
                addNewContact(contactsList[i].getUsername(), i);
            }
        });
    }

    public void addMessage(String sender, String text, boolean isSender) {
        Msg message = new Msg(sender, text, isSender);
        dialogPane.getItems().add(message);
        dialogPane.scrollTo(dialogPane.getItems().size() - 1);
    }

    private void updateContacts() {
        send(new RMessage(RType.CONTACTS, null));
    }

    private void loadLoginForm() throws IOException {
        FormLoader.loadForm("login-form.fxml", new LoginFormController(localData), inputField.getScene());
    }

    private Pane selectedPane;

    private void addNewContact(String name, int id) {
        Pane newPane = new Pane();
        newPane.setPrefSize(200, 48);
        newPane.setStyle("-fx-background-color: #242b33;");

        newPane.setOnMouseClicked(event -> {
            currentSelectedUser = name;
            if(selectedPane != newPane && selectedPane != null) selectedPane.setStyle("-fx-background-color: #242b33;");
            selectedPane = newPane;
            newPane.setStyle("-fx-background-color: #242b40;");
        });

        ImageView imageView = new ImageView(new Image(Application.class.getResourceAsStream("images/avatar.png")));
        imageView.setLayoutX(7); imageView.setLayoutY(3);
        imageView.setFitWidth(42); imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(name);
        nameLabel.setLayoutX(57); nameLabel.setLayoutY(6);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial Bold", 12));

        newPane.getChildren().addAll(imageView, nameLabel);
        newPane.setLayoutY(contactsPane.getChildren().size() * 48);

        if (contactsPane.getChildren().isEmpty()) {
            newPane.setLayoutY(0);
        } else {
            Pane lastPane = (Pane) contactsPane.getChildren().get(contactsPane.getChildren().size() - 1);
            newPane.setLayoutY(lastPane.getLayoutY() + 48 + 5);
        }

        contactsPane.getChildren().add(newPane);
    }

    private void sendChatMessage(String to, String text) throws InvalidKeyException, UntrustedIdentityException, IOException {
        PreKeyBundle receiverKeyBundle = SignalUtility.getContactKeyBundle(to);

        if(receiverKeyBundle != null) {
            SignalSession session = new SignalSession(signalProtocolStore, receiverKeyBundle,
                    new SignalProtocolAddress(to, receiverKeyBundle.getDeviceId()));

            PreKeySignalMessage encryptedMessage = session.encrypt(text);
            ChatMessage chatMessage = new ChatMessage(to, Utility.toBase64(encryptedMessage.serialize()));

            send(new RMessage(RType.MESSAGE, chatMessage));

            addMessage("", text, true);
        }
    }

    private void receiveChatMessage(Object content) throws InvalidMessageException, InvalidVersionException, IOException, InvalidKeyException {
        ChatMessage chatMessage = objectMapper.convertValue(content, ChatMessage.class);
        String username = chatMessage.getUsername();
        PreKeyBundle senderKeyBundle = SignalUtility.getContactKeyBundle(username);
        PreKeySignalMessage encryptedMessage = new PreKeySignalMessage(Utility.fromBase64(chatMessage.getContent()));

        SignalSession session = new SignalSession(SignalUtility.loadLocalProtocolStore(), senderKeyBundle,
                new SignalProtocolAddress(username, senderKeyBundle.getDeviceId()));

        String decryptedMessage  = session.decrypt(encryptedMessage);

        Platform.runLater(() -> {
            addMessage(username, decryptedMessage, false);
        });
    }

    private void updateMessages(String username) {
    }

    private void send(RMessage rMessage) {
        try {
            webSocketClient.sendMessage(objectMapper.writeValueAsString(rMessage));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private class Msg {
        private String sender;
        private String text;
        private boolean isSender;

        public Msg(String sender, String text, boolean isSender) {
            this.sender = sender;
            this.text = text;
            this.isSender = isSender;
        }

        public String getSender() {
            return sender;
        }

        public String getText() {
            return text;
        }

        public boolean isSender() {
            return isSender;
        }
    }
    private class RoundedMessageCell extends ListCell<Msg> {
        @Override
        protected void updateItem(Msg item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                StackPane stackPane = new StackPane();

                Rectangle rectangle = new Rectangle(200, 30);
                rectangle.setArcWidth(20);
                rectangle.setArcHeight(20);

                if (item.isSender()) {
                    rectangle.setFill(Color.rgb(38,87,166));
                } else {
                    rectangle.setFill(Color.rgb(88,118,166));
                }

                Text textNode = new Text(item.getText());
                textNode.setFill(Color.WHITE);

                StackPane.setMargin(textNode, new Insets(5));

                HBox hbox = new HBox();
                hbox.setSpacing(10);
                stackPane.getChildren().addAll(rectangle, textNode);

                if (item.isSender()) {
                    hbox.getChildren().addAll(stackPane);
                    hbox.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    hbox.getChildren().addAll(stackPane);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                }

                setGraphic(hbox);
            }
        }
    }

}

