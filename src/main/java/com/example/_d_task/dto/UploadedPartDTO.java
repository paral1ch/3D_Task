package com.example._d_task.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadedPartDTO {
    private Integer partNumber;
    private String etag;
    private Long size;
}