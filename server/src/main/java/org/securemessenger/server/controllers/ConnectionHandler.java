package org.securemessenger.server.controllers;

import org.securemessenger.server.utility.RepositoryHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class ConnectionHandler implements WebSocketHandler {

    private RepositoryHolder repositoryHolder;
    private ArrayList<WebSocketSession> sessions = new ArrayList<>();

    public ConnectionHandler(RepositoryHolder repositoryHolder) {
        this.repositoryHolder = repositoryHolder;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        RequestHandler requestHandler = new RequestHandler(repositoryHolder, session, sessions);
        requestHandler.handle(message.getPayload().toString());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
