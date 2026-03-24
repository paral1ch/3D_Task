package com.example._d_task.models;


import com.example._d_task.enums.FileStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "file_metadata")
@Data
public class FileMetadataModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer file_id;

    private String s3key;

    private String upload_id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskModel task;
    private String file_name;
    @Enumerated(EnumType.STRING)
    private FileStatus status;

    private LocalDate created_at;

    private LocalDate updated_at;

    @PrePersist
    void onCreate(){
        this.created_at = LocalDate.now();
        this.updated_at = this.created_at;
    }

    @PreUpdate
    void onUpdate(){
        this.updated_at = LocalDate.now();
    }
}
