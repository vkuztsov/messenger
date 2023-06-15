package org.securemessenger.server.utility;

import org.securemessenger.server.repo.ContactRepo;
import org.securemessenger.server.repo.MessageRepo;
import org.securemessenger.server.repo.SessionRepo;
import org.securemessenger.server.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryHolder {
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;
    private final ContactRepo contactRepo;
    private final SessionRepo sessionRepo;

    @Autowired
    public RepositoryHolder(UserRepo userRepo, MessageRepo messageRepo, ContactRepo contactRepo, SessionRepo sessionRepo) {
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.contactRepo = contactRepo;
        this.sessionRepo = sessionRepo;
    }

    public UserRepo getUserRepo() {
        return userRepo;
    }

    public MessageRepo getMessageRepo() {
        return messageRepo;
    }

    public ContactRepo getContactRepo() {
        return contactRepo;
    }

    public SessionRepo getSessionRepo() {
        return sessionRepo;
    }
}

