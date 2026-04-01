package com.example._d_task.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Integer comment_id;

    private String text;

    private Integer task_id;

    private UserDTO user;

}
