module com.securemessenger.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires kotlin.stdlib;
    requires okio;
    requires signal.protocol.java;
    requires curve25519.java;


    opens com.securemessenger.client to javafx.fxml;
    exports com.securemessenger.client;
    exports com.securemessenger.client.signal;
    exports com.securemessenger.client.models.network;
    exports com.securemessenger.client.models.network.structs;
    opens com.securemessenger.client.controllers to javafx.fxml;
}