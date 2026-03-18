package com.example._d_task.models;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "task_comments")
@Data
public class TaskCommentModel {
    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer comment_id;

    @ManyToOne
    private TaskModel task;

    @ManyToOne
    private UserModel user;

    @Column(name = "text")
    private String text;


}
