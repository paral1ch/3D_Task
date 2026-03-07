package com.example._d_task.repositories;

import com.example._d_task.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserModel, Long>{

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserModel u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserModel u WHERE u.username = :username")
    boolean existsByUsername(String username);

    @Query("SELECT u FROM UserModel u WHERE u.email = :email")
    UserModel findByEmail(@Param("email") String email);
}
