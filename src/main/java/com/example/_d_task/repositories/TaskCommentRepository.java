package com.example._d_task.repositories;

import com.example._d_task.models.TaskCommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskCommentModel, Long> {

    @Query("SELECT tcm FROM TaskCommentModel as tcm WHERE tcm.task.task_id = :task_id")
    List<TaskCommentModel> getComments(@Param("task_id") Integer task_id);
}
