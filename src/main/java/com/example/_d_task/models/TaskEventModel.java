package com.example._d_task.models;

import com.example._d_task.enums.TaskEventType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "task_events")
public class TaskEventModel {
    @Id
    @Column(name = "task_event_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer task_event_id;

    @Enumerated(EnumType.STRING)
    @Column(name="event_type")
    private TaskEventType event_type;

    @Column(name="created_at")
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskModel task;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel project;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payload;




}
