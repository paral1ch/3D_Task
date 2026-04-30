package com.example._d_task.repositories;

import com.example._d_task.models.TaskExecutorModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskExecutorRepository extends JpaRepository<TaskExecutorModel, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskExecutorModel tem WHERE tem.user.user_id = :user_id AND tem.task.task_id = :task_id ")
    void deleteExecutor(@Param("task_id") Integer task_id, @Param("user_id") Integer user_id);
}
