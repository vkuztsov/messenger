package org.securemessenger.server.config;

import org.securemessenger.server.controllers.ConnectionHandler;
import org.securemessenger.server.repo.ContactRepo;
import org.securemessenger.server.repo.MessageRepo;
import org.securemessenger.server.repo.SessionRepo;
import org.securemessenger.server.repo.UserRepo;
import org.securemessenger.server.utility.RepositoryHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableJpaRepositories("org.securemessenger.server.repo")
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private SessionRepo sessionRepo;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ConnectionHandler(new RepositoryHolder(userRepo, messageRepo,
                contactRepo, sessionRepo)), "/app");
    }
}
