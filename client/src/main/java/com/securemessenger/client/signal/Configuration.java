package com.securemessenger.client.signal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private Properties properties;
    private String filePath;

    private String comment;

    public Configuration(String filePath, String comment) {
        this.filePath = filePath;
        this.comment = comment;

        properties = new Properties();
        loadConfig();
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public void saveConfig() {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            properties.store(fileOut, comment);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            properties.load(fileIn);
            fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
