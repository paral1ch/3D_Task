package com.example._d_task.services;


import com.example._d_task.dto.NotificationDTO;
import com.example._d_task.models.ProjectNotificationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private UserService userService;

    public List<NotificationDTO> convertModelsToDTO(List<ProjectNotificationModel> list){
        List<NotificationDTO> dtoList = new ArrayList<>();
        for(ProjectNotificationModel notification: list){
            dtoList.add(convertModelToDTO(notification));
        }

        return dtoList;
    }


    public NotificationDTO convertModelToDTO(ProjectNotificationModel notification){
        NotificationDTO dto = new NotificationDTO();
        dto.setNotification_id(notification.getNotification_id());
        dto.setText(notification.getText());
        dto.setDate(notification.getDate());
        dto.setCreated_by(userService.convertModelToDTO(notification.getUser()));
        dto.setReaded(notification.getReaded());
        if(notification.getAdressed_to() != null){
            dto.setAdressed_to(notification.getAdressed_to().getUserDTO());
        }

        return dto;
    }
}
