package com.example._d_task.services;

import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.models.TaskModel;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.stereotype.Service;


@Service
public class PermisssionService {
    public final TaskRepository taskRepository;
    public final UserProjectRepository userProjectRepository;

    public PermisssionService(TaskRepository taskRepository,
                              UserProjectRepository userRepository){
        this.taskRepository = taskRepository;
        this.userProjectRepository = userRepository;
    }

    public boolean canAddComments(Integer task_id){
        TaskModel task = taskRepository.findById(task_id);
        if(!taskRepository.getExecutors(task_id).contains(Auth.user()) &&
                !taskRepository.getVerifiers(task_id).contains(Auth.user()) &&
                !ProjectRolePermissions.canCreateTask(
                        userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), task.getProject().getProject_id()))){
            return false;

        }
        return true;
    }
}
