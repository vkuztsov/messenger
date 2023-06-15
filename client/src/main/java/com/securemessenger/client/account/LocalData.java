package com.securemessenger.client.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LocalData {
    private File localData;
    private String password;

    private Properties properties;

    public LocalData(File localData) {
        this.localData = localData;
        properties = new Properties();
    }

    public void setSession(String session) {
        writeProperty("session", session);
    }

    public String getSession() {
        return readProperty("session");
    }

    public void setUsername(String username) {
        writeProperty("username", username);
    }

    public String getUsername() {
        return readProperty("username");
    }

    private void writeProperty(String key, String value) {
        try {
            properties.setProperty(key, value);
            FileOutputStream outputStream = new FileOutputStream(localData);
            properties.store(outputStream, "Local Data");
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readProperty(String key) {
        try {
            FileInputStream inputStream = new FileInputStream(localData);
            properties.load(inputStream);
            inputStream.close();

            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
