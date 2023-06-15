package org.securemessenger.server.repo;

import jakarta.transaction.Transactional;
import org.securemessenger.server.models.entities.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepo extends CrudRepository<Session, Integer> {
    @Query("SELECT s.userId FROM Session s WHERE s.authKey = :authKey")
    Integer findUserIdByAuthKey(@Param("authKey") String authKey);

    @Transactional
    void deleteAllById(Integer id);
}