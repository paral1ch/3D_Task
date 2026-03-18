package com.example._d_task.repositories;

import com.example._d_task.models.TaskVerifierModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskVerifierRepository  extends JpaRepository<TaskVerifierModel, Long> {
}
