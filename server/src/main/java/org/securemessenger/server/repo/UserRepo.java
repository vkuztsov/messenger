package org.securemessenger.server.repo;

import jakarta.transaction.Transactional;
import org.securemessenger.server.models.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends CrudRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByUsernameAndPassword(String username, String password);

    @Query("SELECT u.id FROM User u WHERE u.username = ?1")
    Integer findIdByUsername(String username);

    @Query("SELECT u.publicKey FROM User u WHERE u.id = :id")
    String findPublicKeyById(@Param("id") Integer id);

    @Query("SELECT u.photoUrl FROM User u WHERE u.id = :id")
    String findPhotoUrlById(@Param("id") Integer id);

    @Query("SELECT u.username FROM User u WHERE u.id = :id")
    String findUsernameById(@Param("id") Integer id);
}
