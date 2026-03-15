package com.example._d_task.services;

import com.example._d_task.dto.TaskDTO;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskServices {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    public void createTask(Integer project_id, TaskDTO taskDTO){
        TaskModel task = new TaskModel();
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());
        task.setParent_task_id(taskDTO.getParent_task_id());
        task.setUser(Auth.user());
        task.setStatus(TaskEnum.IN_PROGRESS);
        task.setProject(projectRepository.findByProjectId(project_id));
        taskRepository.save(task);
    }

}
