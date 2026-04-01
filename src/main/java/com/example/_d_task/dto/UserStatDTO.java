package com.example._d_task.dto;


import lombok.Data;

import java.util.List;

@Data
public class UserStatDTO {
    private UserDTO user;

    private List<TaskDTO> verifying;
    private List<TaskDTO> executing;
}
