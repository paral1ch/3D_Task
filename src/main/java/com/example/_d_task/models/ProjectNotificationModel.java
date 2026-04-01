package com.example._d_task.models;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "project_notifications")
public class ProjectNotificationModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer notification_id;

    @Column(name = "text")
    private String text;


    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel project;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Column(name = "date")
    private LocalDate date;

}
