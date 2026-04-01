package com.example._d_task.repositories;

import com.example._d_task.models.ProjectNotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectNotificationRepository extends JpaRepository<ProjectNotificationModel, Long> {
}
