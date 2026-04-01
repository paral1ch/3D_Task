package com.example._d_task.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationDTO {
    private UserDTO created_by;

    private String text;

    private Integer notification_id;

    private Integer project_id;

    private LocalDate date;
}
