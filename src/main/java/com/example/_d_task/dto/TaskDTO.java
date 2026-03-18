package com.example._d_task.dto;


import com.example._d_task.enums.TaskEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

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
    @Nullable
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate deadline;

    public TaskDTO(){}

    public TaskDTO(Integer task_id, Integer parent_task_id,Integer project_id,
                   Integer user_id,String name, String description, TaskEnum status,LocalDate deadline){
        this.task_id=task_id;
        this.parent_task_id=parent_task_id;
        this.project_id=project_id;
        this.user_id=user_id;
        this.name=name;
        this.description=description;
        this.status=status;
        this.deadline=deadline;

    }
}
