package com.example._d_task.models;


import com.example._d_task.models.Id.TaskExecutorModelId;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "task_executor")
@Data
@IdClass(TaskExecutorModelId.class)
public class TaskExecutorModel {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Id
    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskModel task;

    @Column(name = "assigned_at")
    private LocalDate assigned_at;
}
