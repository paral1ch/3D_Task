package com.example._d_task.services;

import com.example._d_task.dto.TaskDTO;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void deleteTask(Integer task_id){
        TaskModel taskOrig = taskRepository.findById(task_id);
        List<TaskModel> tasks = taskRepository.findTaskAsParent(task_id);
        if(!tasks.isEmpty()){
            for (TaskModel task : tasks) {
                task.setParent_task_id(taskOrig.getParent_task_id());
                taskRepository.save(task);
            }

        }
        taskRepository.delete(taskOrig);
    }

    public TaskDTO taskToDTO(TaskModel task){
        TaskDTO dto = new TaskDTO();
        dto.setTask_id(task.getTask_id());
        dto.setDescription(task.getDescription());
        dto.setName(task.getName());
        dto.setStatus(task.getStatus());
        dto.setUser_id(task.getUser().getUserId());
        dto.setProject_id(task.getProject().getProject_id());
        dto.setParent_task_id(task.getParent_task_id());
        return dto;
    }
}
