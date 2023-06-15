package org.securemessenger.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.securemessenger.server.models.entities.Contact;
import org.securemessenger.server.models.entities.Message;
import org.securemessenger.server.models.entities.Session;
import org.securemessenger.server.models.entities.User;
import org.securemessenger.server.models.network.RMessage;
import org.securemessenger.server.models.network.RType;
import org.securemessenger.server.models.network.structs.*;
import org.securemessenger.server.repo.ContactRepo;
import org.securemessenger.server.repo.MessageRepo;
import org.securemessenger.server.repo.SessionRepo;
import org.securemessenger.server.repo.UserRepo;
import org.securemessenger.server.utility.Crypto;
import org.securemessenger.server.utility.RepositoryHolder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler {
    private RepositoryHolder repositoryHolder;
    private WebSocketSession session;
    private ArrayList<WebSocketSession> sessions;

    private ObjectMapper objectMapper;

    public RequestHandler(RepositoryHolder repositoryHolder, WebSocketSession session, ArrayList<WebSocketSession> sessions) {
        this.repositoryHolder = repositoryHolder;
        this.session = session;
        this.sessions = sessions;

        this.objectMapper = new ObjectMapper();
    }

    public void handle(String request) {
        try {
            RMessage message = objectMapper.readValue(request, RMessage.class);
            RType type = message.getType();
            Object content = message.getContent();

            if(!authorized()) {
                switch (type) {
                    case AUTHENTICATION -> authentication(content);
                    case AUTHORIZATION -> authorization(content);
                    case REGISTRATION -> registration(content);
                }

                return;
            }

            switch (type) {
                case ACCOUNT_DATA -> accountData();
                case USER_DATA -> userData(content);
                case ADD_CONTACT -> addContact(content);
                case CONTACTS -> contacts();
                case MESSAGE -> message(content);
                case GET_MESSAGES -> getMessages(content);
                case EXIT -> accountExit();
            }
        } catch (IOException e) {
            sendError("Bad request");
        }

    }

    private void getMessages(Object content) {
        Username username = objectMapper.convertValue(content, Username.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();

        Integer contactId = userRepo.findIdByUsername(username.getUsername());
        Integer userId = (Integer) session.getAttributes().get("USER_ID");

        if(contactId != null) {

            MessageRepo messageRepo = repositoryHolder.getMessageRepo();
            List<Message> messagesList = messageRepo.findTop30ByChatIdOrderBySentDateDesc(getChatId(contactId, userId));

            if(!messagesList.isEmpty()) {
                ChatMessage[] chatMessages = new ChatMessage[messagesList.size()];
                for(int i = 0; i < messagesList.size(); i++) {
                    Message message = messagesList.get(i);
                    ChatMessage chatMessage = new ChatMessage(userRepo.findUsernameById(message.getSenderId()),
                            message.getContent());
                    chatMessages[i] = chatMessage;
                }

                sendResponse(new RMessage(RType.GET_MESSAGES, chatMessages));
            }

        }else {
            sendError("User not found");
        }
    }

    private void message(Object content) {
        ChatMessage chatMessage = objectMapper.convertValue(content, ChatMessage.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();
        MessageRepo messageRepo = repositoryHolder.getMessageRepo();

        Integer receiverId = userRepo.findIdByUsername(chatMessage.getUsername());
        Integer senderId = (Integer) session.getAttributes().get("USER_ID");

        if(receiverId != null) {
            String chatId = getChatId(senderId, receiverId);

            Message message = new Message(senderId, receiverId,
                    chatMessage.getContent(), chatId, LocalDateTime.now());
            messageRepo.save(message);

            chatMessage.setUsername(userRepo.findUsernameById(senderId));
            sendMessage(chatMessage, receiverId);
        }else{
            sendError("User not found");
        }
    }

    private void contacts() {
        ContactRepo contactRepo = repositoryHolder.getContactRepo();
        List<Integer> contactsList = contactRepo.findContactIdsByUserId((Integer) session.getAttributes().get("USER_ID"));
        if(!contactsList.isEmpty()) {
            UserData[] usersData = new UserData[contactsList.size()];

            for(int i = 0; i < contactsList.size(); i++) {
                usersData[i] = getUserData(contactsList.get(i));
            }

            Contacts contacts = new Contacts(usersData);
            sendResponse(new RMessage(RType.CONTACTS, contacts));
        }
    }

    private void userData(Object content) {
        Username user = objectMapper.convertValue(content, Username.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();

        Integer userId = userRepo.findIdByUsername(user.getUsername());

        if(userId != null) {
            sendResponse(new RMessage(RType.USER_DATA, getUserData(userId)));
        }else{
            sendError("User not found");
        }
    }

    private void accountExit() {
        SessionRepo sessionRepo = repositoryHolder.getSessionRepo();
        sessionRepo.deleteAllById((Integer) session.getAttributes().get("USER_ID"));

        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addContact(Object content) {
        Username contact = objectMapper.convertValue(content, Username.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();

        Integer contactId = userRepo.findIdByUsername(contact.getUsername());

        if(contactId != null) {
            Integer id = (Integer) session.getAttributes().get("USER_ID");
            ContactRepo contactRepo = repositoryHolder.getContactRepo();
            Contact newContact = new Contact(id, contactId);
            contactRepo.save(newContact);
        }else{
            sendError("User not found");
        }
    }

    private void accountData() {
        Integer id = (Integer) session.getAttributes().get("USER_ID");
        UserRepo userRepo = repositoryHolder.getUserRepo();

        UserData userData = new UserData(userRepo.findUsernameById(id),
                userRepo.findPhotoUrlById(id), null);

        RMessage message = new RMessage(RType.ACCOUNT_DATA, userData);

        sendResponse(message);
    }

    private void authorization(Object content) {
        if(authorized()) return;
        Authorization auth = objectMapper.convertValue(content, Authorization.class);
        SessionRepo sessionRepo = repositoryHolder.getSessionRepo();

        Integer id = sessionRepo.findUserIdByAuthKey(auth.getKey());

        if(id != null) {
            session.getAttributes().put("USER_ID", id);
            sendResponse(new RMessage(RType.SERVER_MESSAGE, "AUTH_SUCCESS"));
        }else{
            sendResponse(new RMessage(RType.SERVER_MESSAGE, "SESSION_NOT_FOUND"));
        }
    }

    private void authentication(Object content) {
        if(authorized()) return;

        Authentication auth = objectMapper.convertValue(content, Authentication.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();
        SessionRepo sessionRepo = repositoryHolder.getSessionRepo();

        if(userRepo.existsByUsernameAndPassword(auth.getUsername(), Crypto.sha256(auth.getPassword())))
        {
            String sessionKey = Crypto.sha256(Crypto.generateRandomString(32));
            int id = userRepo.findIdByUsername(auth.getUsername());

            Session newSession = new Session(id, sessionKey, LocalDateTime.now());
            sessionRepo.save(newSession);

            RMessage message = new RMessage(RType.SESSION_KEY, sessionKey);
            sendResponse(message);

        }else {
            sendError("Incorrect username or password");
        }
    }

    private void registration(Object content) {
        if(authorized()) return;

        Registration registration = objectMapper.convertValue(content, Registration.class);
        UserRepo userRepo = repositoryHolder.getUserRepo();

        if(!validateRegistration(registration)) {
            sendError("Data has not been validated");
            return;
        }

        if(userRepo.existsByUsername(registration.getUsername())) {
            sendError("User with the same name already exists");
        } else {
            User user = new User(registration.getUsername(), Crypto.sha256(registration.getPassword()), null, null,
                    registration.getPublicKey(), session.getRemoteAddress().getHostString(), LocalDateTime.now());

            userRepo.save(user);
            sendResponse(new RMessage(RType.SERVER_MESSAGE, "REG_SUCCESS"));
        }
    }

    private boolean validateRegistration(Registration registration) {
        if (registration.getUsername() == null || registration.getUsername().isEmpty() || registration.getUsername().length() < 4) {
            return false;
        }
        if (registration.getPassword() == null || registration.getPassword().isEmpty() || registration.getPassword().length() < 6) {
            return false;
        }
        if (registration.getPublicKey() == null || registration.getPublicKey().isEmpty()) {
            return false;
        }

        return true;
    }

    private UserData getUserData(Integer userId) {
        UserRepo userRepo = repositoryHolder.getUserRepo();

        UserData userData = new UserData(userRepo.findUsernameById(userId), userRepo.findPhotoUrlById(userId),
                userRepo.findPublicKeyById(userId));

        return userData;
    }

    private void sendError(String text) {
        sendResponse(new RMessage(RType.ERROR, text));
    }

    private void sendResponse(RMessage message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getChatId(Integer firstUser, Integer secondUser) {
        String id = "id";
        if(firstUser > secondUser) {
            id += firstUser + "_" + secondUser;
        }else {
            id += secondUser + "_" + firstUser;
        }

        return id;
    }

    private void sendMessage(ChatMessage chatMessage, Integer receiverId) {
                try {
                    for(int i = 0; i < sessions.size(); i++) {
                        WebSocketSession otherSession = sessions.get(i);
                        if (otherSession.getAttributes().get("USER_ID") == receiverId) {
                            RMessage message = new RMessage(RType.MESSAGE, chatMessage);
                            otherSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
    }

    private boolean authorized() {
        return session.getAttributes().get("USER_ID") != null;
    }

}
