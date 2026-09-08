package com.example._d_task.services;


import com.example._d_task.dto.NotificationDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.models.ProjectNotificationModel;
import com.example._d_task.repositories.ProjectNotificationRepository;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class NotificationService {


    private final ProjectRepository projectRepository;
    private final ProjectNotificationRepository projectNotificationRepository;
    private final UserProjectRepository userProjectRepository;
    public NotificationService(UserService userService, ProjectServices projectServices, ProjectRepository projectRepository,
                               ProjectNotificationRepository projectNotificationRepository, UserProjectRepository userProjectRepository){
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
        this.projectNotificationRepository = projectNotificationRepository;
    }




    public ResponseEntity<?> addNotification(@PathVariable("project_id") Integer project_id,
                                             @RequestBody NotificationDTO dto){
        if (!ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        ProjectNotificationModel projectNotification = new ProjectNotificationModel();
        projectNotification.setText(dto.getText());
        projectNotification.setProject(projectRepository.findByProjectId(project_id));
        projectNotification.setUser(Auth.user());
        projectNotification.setDate(dto.getDate());
        projectNotificationRepository.save(projectNotification);

        return ResponseEntity.ok("Notification created");
    }
}
