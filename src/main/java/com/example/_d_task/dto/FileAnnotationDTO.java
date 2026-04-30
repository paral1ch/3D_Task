package com.example._d_task.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class FileAnnotationDTO {

    private Integer id;

    private String s3Key;

    private FileDTO file;

    private UserDTO created_by;

    private LocalDate created_at;

    private Object payload;

}
