package com.example._d_task.services.servicesUtils;


import com.example._d_task.dto.*;
import com.example._d_task.models.*;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskCommentRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelToDTOConverters {
    private final UserProjectRepository userProjectRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final JSONPayloadNormalizer jsonPayloadNormalizer;

    public ModelToDTOConverters(UserProjectRepository userProjectRepository,
                                TaskRepository taskRepository,
                                ProjectRepository projectRepository,
                                TaskCommentRepository taskCommentRepository,
                                JSONPayloadNormalizer jsonPayloadNormalizer){
        this.userProjectRepository = userProjectRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.jsonPayloadNormalizer = jsonPayloadNormalizer;
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
        dto.setDeadline(task.getDeadline());
        dto.setVerifiers(usersToDTO(taskRepository.getVerifiers(dto.getTask_id())));
        dto.setExecutors(usersToDTO(taskRepository.getExecutors(dto.getTask_id())));
        dto.setComments(commentsToDTO(taskCommentRepository.getComments(task.getTask_id())));
        return dto;
    }

    public List<ProjectDTO> projectsToDTO(List<ProjectModel> projects){
        return projects.stream().map(
                project -> new ProjectDTO(project,userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project.getProject_id()))
        ).collect(Collectors.toList());
    }

    public ProjectModel DTOToProject(ProjectDTO projectDTO){
        UserModel user = Auth.user();
        ProjectModel project = new ProjectModel();
        project.setUser_id(user.getUserId());
        project.setProjectDescription(projectDTO.getProject_description());
        project.setName(projectDTO.getProject_name());

        projectRepository.save(project);
        return project;
    }

    public List<TaskDTO> tasksToDTO(List<TaskModel> tasks){
        List<TaskDTO> list = new ArrayList<>();
        for(TaskModel task :tasks){
            list.add(taskToDTO(task));
        }
        return list;
    }

    public CommentDTO commentToDTO(TaskCommentModel taskCommentModel){
        CommentDTO dto = new CommentDTO();
        dto.setComment_id(taskCommentModel.getComment_id());
        dto.setText(taskCommentModel.getText());
        dto.setUser(taskCommentModel.getUser().getUserDTO());
        dto.setTask_id(taskCommentModel.getTask().getTask_id());
        return dto;
    }

    public List<CommentDTO> commentsToDTO(List<TaskCommentModel> comments){
        List<CommentDTO> dtoList = new ArrayList<>();

        for(TaskCommentModel comment: comments){
            dtoList.add(commentToDTO(comment));
        }
        return dtoList;
    }

    public UserDTO userToDTO(UserModel user){
        return new UserDTO(user);
    }

    public List<UserDTO> usersToDTO(List<UserModel> users){
        return users.stream().map(
                UserDTO::new
        ).collect(Collectors.toList());
    }

    public List<UserDTO> usersInProjectToDTO(List<UserModel> users, Integer project_id){
        return users.stream().map(
                user -> new UserDTO(user,
                        userProjectRepository.findRolesByUserAndProject(user.getUserId(),project_id))
        ).collect(Collectors.toList());
    }

    public List<NotificationDTO> notificationsToDTO(List<ProjectNotificationModel> list){
        List<NotificationDTO> dtoList = new ArrayList<>();
        for(ProjectNotificationModel notification: list){
            dtoList.add(notificationToDTO(notification));
        }

        return dtoList;
    }

    public NotificationDTO notificationToDTO(ProjectNotificationModel notification){
        NotificationDTO dto = new NotificationDTO();
        dto.setNotification_id(notification.getNotification_id());
        dto.setText(notification.getText());
        dto.setDate(notification.getDate());
        dto.setCreated_by(userToDTO(notification.getUser()));
        dto.setReaded(notification.getReaded());
        if(notification.getAdressed_to() != null){
            dto.setAdressed_to(notification.getAdressed_to().getUserDTO());
        }
        else{dto.setAdressed_to(null);}

        return dto;
    }


    public TaskEventDTO eventToDTO(TaskEventModel event) {
        TaskEventDTO dto = new TaskEventDTO();
        dto.setEvent_id(event.getTask_event_id());
        dto.setEventType(event.getEvent_type());
        dto.setProject_id(event.getProject().getProject_id());
        dto.setTask(taskToDTO(event.getTask()));
        dto.setCreated_at(event.getCreated_at());
        dto.setUser(userToDTO(event.getUser()));
        JsonNode normalizedPayload = jsonPayloadNormalizer.normalizePayload(event.getPayload());
        dto.setPayload(
                jsonPayloadNormalizer.isCorruptedPayloadDescriptor(normalizedPayload) || normalizedPayload == null
                        ? null
                        : normalizedPayload.toString()
        );
        return dto;
    }

    public List<TaskEventDTO> eventsToDTO(List<TaskEventModel> list){
        List<TaskEventDTO> dtos = new ArrayList<>();
        list.forEach(event -> dtos.add(eventToDTO(event)));
        return dtos;
    }
}
