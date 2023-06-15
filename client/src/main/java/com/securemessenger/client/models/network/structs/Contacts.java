package com.securemessenger.client.models.network.structs;

public class Contacts {
    private UserData[] contacts;

    public Contacts(UserData[] contacts) {
        this.contacts = contacts;
    }

    public Contacts() {}

    public UserData[] getContacts() {
        return contacts;
    }

    public void setContacts(UserData[] contacts) {
        this.contacts = contacts;
    }
}
