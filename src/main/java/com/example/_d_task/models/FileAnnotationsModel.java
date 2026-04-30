package com.example._d_task.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "file_annotations")
@Data
public class FileAnnotationsModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileMetadataModel file;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserModel created_by;

    private LocalDate created_at;

    @Column(name =  "s3key")
    private String s3Key;


}
