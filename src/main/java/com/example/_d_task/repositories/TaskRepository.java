package com.example._d_task.repositories;

import com.example._d_task.models.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository  extends JpaRepository<TaskModel, Long> {

    @Query("SELECT t FROM TaskModel as t WHERE t.task_id = :task_id AND t.project.project_id = :project_id")
    TaskModel findByProjectIdAndTaskID(@Param("task_id") Integer task_id, @Param("project_id") Integer project_id);
}
