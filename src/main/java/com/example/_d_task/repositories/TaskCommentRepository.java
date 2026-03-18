package com.example._d_task.repositories;

import com.example._d_task.models.TaskCommentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskCommentModel, Long> {
}
