package com.example._d_task.repositories;


import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.models.UserModel;
import com.example._d_task.models.UserProjectModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProjectModel, Long> {

    @Query("SELECT up.project_role FROM UserProjectModel as up WHERE :user_id=up.user.user_id AND :project_id=up.project.project_id")
    List<ProjectRoles> findRolesByUserAndProject(@Param("user_id") Integer user_id, @Param("project_id") Integer project_id);

    @Query("SELECT u FROM UserModel as u where u.user_id in (SELECT up.user.user_id FROM UserProjectModel as up where up.project.project_id = :project_id)")
    List<UserModel> getUsersFromProject(@Param("project_id") Integer project_id);

    @Query("SELECT pm FROM ProjectModel as pm where pm.user_id in (SELECT DISTINCT upm.user.user_id FROm UserProjectModel as upm where upm.user.user_id = :user_id)")
    List<ProjectModel> getProjectsFromUser(@Param("user_id") Integer user_id);

    @Query("SELECT upm.project FROM UserProjectModel as upm WHERE upm.user.user_id = :user_id")
    List<ProjectModel> getUserProjects(@Param("user_id") Integer user_id);

    @Query("SELECT upm FROM UserProjectModel as upm where :user_id = upm.user.user_id and :project_id = upm.project.project_id")
    List<UserProjectModel> getUserProjects(@Param("user_id") Integer user_id, @Param("project_id") Integer project_id);

    @Query("SELECT upm FROM UserProjectModel as upm where :email = upm.user.email and :project_id = upm.project.project_id")
    List<UserProjectModel> getUserProjects(@Param("email") String email, @Param("project_id") Integer project_id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserProjectModel up WHERE up.user.user_id = :user_id AND up.project.project_id = :project_id")
    void deleteByUserIdAndProjectId(@Param("user_id")Integer user_id,@Param("project_id") Integer project_id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserProjectModel up WHERE up.user.user_id = :user_id AND up.project.project_id = :project_id AND up.project_role = :project_role")
    void deleteByUserIdAndProjectIdAndRole(@Param("user_id")Integer user_id,@Param("project_id") Integer project_id
    ,@Param("project_role") ProjectRoles project_role);
}
