package com.example._d_task.models;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "project_files")
@Data
public class ProjectFilesModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer file_id;

    private String s3key;

    private String file_name;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel project;

}
