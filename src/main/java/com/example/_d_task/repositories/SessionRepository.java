package com.example._d_task.repositories;


import com.example._d_task.models.SessionModel;
import com.example._d_task.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionModel, Long> {

    @Query("SELECT s FROM SessionModel s WHERE s.email = :email")
    SessionModel findByEmail(@Param("email") String email);

    @Query("SELECT s FROM SessionModel s WHERE s.session_token = :token")
    SessionModel findByToken(@Param("token") String token);

    @Query("SELECT u FROM UserModel u WHERE u.email = (SELECT s.email FROM SessionModel s WHERE s.session_token = :token)")
    UserModel findUserByToken(@Param("token") String token);
}
