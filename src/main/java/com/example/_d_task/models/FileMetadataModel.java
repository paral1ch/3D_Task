package com.example._d_task.models;


import com.example._d_task.enums.FileStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;

@Entity
@Table(name = "file_metadata")
@Data
public class FileMetadataModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer file_id;

    private String s3key;

    private String upload_id;

    private Integer asset_id;
    private Integer version;
    private Boolean verifier_file;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskModel task;
    private String file_name;
    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @OneToMany(mappedBy = "file",cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<FileAnnotationsModel> annotations;

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
