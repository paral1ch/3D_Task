package com.example._d_task.dto;


import com.example._d_task.enums.TaskEnum;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class TaskDTO {
    @Nullable
    private Integer task_id;
    @Nullable
    private Integer parent_task_id;
    @Nullable
    private Integer project_id;
    @Nullable
    private Integer user_id;
    @Nullable
    private String name;
    @Nullable
    private String description;
    @Nullable
    private TaskEnum status;
    public TaskDTO(){}
}
