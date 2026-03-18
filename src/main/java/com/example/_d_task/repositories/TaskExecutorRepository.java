package com.example._d_task.repositories;

import com.example._d_task.models.TaskExecutorModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskExecutorRepository extends JpaRepository<TaskExecutorModel, Long> {
}
