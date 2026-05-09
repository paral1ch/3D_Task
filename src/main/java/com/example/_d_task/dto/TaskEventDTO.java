package com.example._d_task.dto;

import com.example._d_task.enums.TaskEventType;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class TaskEventDTO {
    private Integer event_id;

    private LocalDateTime created_at;

    private TaskEventType eventType;

    private TaskDTO task;

    private Integer project_id;

    private UserDTO user;

    private String payload;
}
