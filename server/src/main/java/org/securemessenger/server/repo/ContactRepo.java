package org.securemessenger.server.repo;

import org.securemessenger.server.models.entities.Contact;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepo extends CrudRepository<Contact, Integer> {
    @Query("SELECT c.contactId FROM Contact c WHERE c.userId = :userId")
    List<Integer> findContactIdsByUserId(@Param("userId") Integer userId);
}
