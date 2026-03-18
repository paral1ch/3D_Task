package com.example._d_task.models;


import com.example._d_task.enums.TaskEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
@Entity
@Table(name = "tasks")
public class TaskModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer task_id;

    @Column(name = "parent_task_id")
    private Integer parent_task_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel project;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskEnum status;

    @Column(name = "deadline")
    private LocalDate deadline;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskExecutorModel> task_executors;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskVerifierModel> task_verifiers;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskCommentModel> comments;

    public TaskModel(){}


}
