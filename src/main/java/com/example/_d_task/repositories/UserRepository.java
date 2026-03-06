package com.example._d_task.repositories;

import com.example._d_task.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserModel, Long>{
    boolean existsByEmail(String email);
    boolean existsByDisplay_name(String display_name);
}
