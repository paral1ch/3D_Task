package com.example._d_task.repositories;


import com.example._d_task.models.SessionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionModel, Long> {




    @Query("SELECT s FROM SessionModel s WHERE s.refresh_token = :token")
    SessionModel findByRefreshToken(@Param("token") String token);

    @Query("SELECT s FROM SessionModel s WHERE s.access_token = :token")
    SessionModel findByAccessToken(@Param("token") String token);

}
