package com.example._d_task.repositories;


import com.example._d_task.ENUMS.ProjectRoles;
import com.example._d_task.models.UserProjectModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProjectModel, Long> {

    @Query("SELECT up.project_role FROM UserProjectModel as up WHERE :user_id=up.user_id AND :project_id=up.project_id")
    List<ProjectRoles> findRolesByUserAndProject(@Param("user_id") Integer user_id, @Param("project_id") Integer project_id);
}
