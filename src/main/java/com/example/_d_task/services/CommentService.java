package com.example._d_task.services;


import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.models.TaskModel;
import com.example._d_task.repositories.TaskCommentRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserProjectRepository userProjectRepository;
    public CommentService(TaskCommentRepository taskCommentRepository, TaskRepository taskRepository,
                          UserProjectRepository userProjectRepository){
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userProjectRepository = userProjectRepository;

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
