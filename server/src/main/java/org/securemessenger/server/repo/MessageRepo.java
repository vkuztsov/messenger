package org.securemessenger.server.repo;

import org.securemessenger.server.models.entities.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends CrudRepository<Message, Integer> {
    List<Message> findTop30ByChatIdOrderBySentDateDesc(String chatId);
}
