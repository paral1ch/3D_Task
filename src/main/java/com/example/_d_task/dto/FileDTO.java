package com.example._d_task.dto;

import com.example._d_task.enums.FileStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FileDTO {
    private Integer file_id;
    private String file_name;
    private Integer task_id;
    private FileStatus status;
    private LocalDate created_at;
    private LocalDate updated_at;
    private String upload_id;
}