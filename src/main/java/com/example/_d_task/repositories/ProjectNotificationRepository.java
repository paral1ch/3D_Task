package com.example._d_task.repositories;

import com.example._d_task.models.ProjectNotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectNotificationRepository extends JpaRepository<ProjectNotificationModel, Long> {
    @Query("SELECT pnm FROM ProjectNotificationModel as pnm where pnm.adressed_to.user_id = :user_id AND pnm.project.project_id = :project_id")
    List<ProjectNotificationModel> getNotificationFromUser(@Param("user_id") Integer user_id, @Param("project_id") Integer project_id);

    @Query("SELECT pnm FROM ProjectNotificationModel as pnm where pnm.notification_id = :notification_id")
    ProjectNotificationModel getNotificationById( @Param("notification_id") Integer notification_id);

}
