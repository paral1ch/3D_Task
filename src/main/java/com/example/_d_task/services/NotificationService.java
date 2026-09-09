package com.example._d_task.services;


import com.example._d_task.dto.NotificationDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.models.ProjectNotificationModel;
import com.example._d_task.repositories.ProjectNotificationRepository;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {


    private final ProjectRepository projectRepository;
    private final ProjectNotificationRepository projectNotificationRepository;
    private final UserProjectRepository userProjectRepository;
    private final ModelToDTOConverters modelToDTOConverters;
    public NotificationService( ProjectRepository projectRepository,
                               ProjectNotificationRepository projectNotificationRepository,
                                UserProjectRepository userProjectRepository,
                               ModelToDTOConverters modelToDTOConverters){
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
        this.projectNotificationRepository = projectNotificationRepository;
        this.modelToDTOConverters = modelToDTOConverters;
    }




    public ResponseEntity<?> addNotification( Integer project_id,
                                             NotificationDTO dto){
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

    public ResponseEntity<?> getNotifications(Integer project_id){
        //List<ProjectNotificationModel> notList = (List<ProjectNotificationModel>) projectRepository.findByProjectId(project_id).getNotifications();
        List<ProjectNotificationModel> notList = projectNotificationRepository.getFromProject(project_id);

        List<NotificationDTO> list = modelToDTOConverters.notificationsToDTO(notList);
        list.removeIf(dto -> dto.getAdressed_to() != null);
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<?> setReaded(Integer project_id,  Integer notification_id){
        if(!projectNotificationRepository.getNotificationById(notification_id).getAdressed_to().equals(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden action");
        }
        ProjectNotificationModel pnm = projectNotificationRepository.getNotificationById(notification_id);
        pnm.setReaded(true);
        projectNotificationRepository.save(pnm);

        return ResponseEntity.ok("Readed");
    }
}
