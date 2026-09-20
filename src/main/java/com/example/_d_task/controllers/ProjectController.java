package com.example._d_task.controllers;


import com.example._d_task.dto.InviteDTO;
import com.example._d_task.dto.NotificationDTO;
import com.example._d_task.dto.ProjectDTO;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.*;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping(path="/project")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final ProjectServices projectService;
    private final ProjectNotificationRepository projectNotificationRepository;
    private final NotificationService notificationService;
    private final UserPerformanceService userPerformanceService;
    private final ModelToDTOConverters modelToDTOConverters;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Logger log = Logger.getLogger(ProjectController.class.getName());

    public ProjectController(ProjectRepository projectRepository,
                             UserProjectRepository userProjectRepository,
                             ProjectServices projectService,
                             ProjectNotificationRepository projectNotificationRepository,
                             NotificationService notificationService,
                             ModelToDTOConverters modelToDTOConverters,
                             UserPerformanceService userPerformanceService){
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
        this.projectService = projectService;
        this.projectNotificationRepository = projectNotificationRepository;
        this.notificationService = notificationService;
        this.modelToDTOConverters = modelToDTOConverters;
        this.userPerformanceService = userPerformanceService;
    }

    @GetMapping(path = "/myProjects")
    public ResponseEntity<?> getProjects(){
        UserModel user = Auth.user();
        //log.info(projectService.convertModelsToDTOInProject(userProjectRepository.getProjectsFromUser(user.getUserId())).toString());
        return ResponseEntity.ok(modelToDTOConverters.projectsToDTO(userProjectRepository.getUserProjects(user.getUserId())));
    }


    @GetMapping(path = "/{project_id}")
    public ResponseEntity<?> getProject(@PathVariable Integer project_id){
        return projectService.getProject(project_id);
    }


    @PostMapping(path = "/create")
    public ResponseEntity<String> createProject(@RequestBody ProjectDTO projectDTO){

        return projectService.createProject(projectDTO);
    }

    @PostMapping(path = "/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteDTO inviteDTO){
        return projectService.inviteToProject(inviteDTO);
    }


    @GetMapping(path = "/myRoles/{project_id}")
    public ResponseEntity<?> myRoles(@PathVariable Integer project_id){
        return ResponseEntity.ok(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id)
                +" " + Auth.user() + " "+  project_id);
    }

    @PostMapping(path = "/{project_id}/edit")
    public ResponseEntity<?> editProject(@PathVariable Integer project_id, @RequestBody ProjectDTO projectDTO){
        return projectService.editProject(project_id,projectDTO);
    }

    @GetMapping(path = "/{project_id}/users")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id){
        return projectService.getProjectUsers(project_id);
    }
    // Ваще не помню че за эндпоинт и почему у него такой странный путь, потом вспомнить и перелопатить\удалить
    //@GetMapping(path = "/{project_id}/users/edit")
    //public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id, UserDTO userDTO){
    //    if(ProjectRolePermissions.canModifyProject(userProjectRepository.
    //            findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
    //        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
    //    }
    //    return ResponseEntity.ok(userService.convertModelsToDTOInProject(
    //            userProjectRepository.getUsersFromProject(project_id),project_id
    //    ));
    //}

    @PostMapping(path = "/{project_id}/delete")
    public ResponseEntity<?> deleteProject(@PathVariable Integer project_id){
        return projectService.deleteProject(project_id);
    }

    @PostMapping("/{project_id}/deleteUser/{user_id}")
    public ResponseEntity<?> deleteUserFromProject(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){
        return projectService.deleteUserFromProject(project_id,user_id);
    }

    @PostMapping("/{project_id}/addRole/{user_id}")
    public ResponseEntity<?> addRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
        return projectService.addRole(project_id,user_id);
    }

    @PostMapping("/{project_id}/deleteRole/{user_id}")
    public ResponseEntity<?> deleteRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
        return projectService.deleteRole(project_id,user_id);
    }

    @GetMapping("/{project_id}/getTasks")
    public ResponseEntity<?> getTasks(@PathVariable("project_id") Integer project_id){
        if(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id) ==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cant get tasks from that project");
        }

        return ResponseEntity.ok(modelToDTOConverters.tasksToDTO(projectRepository.findTasks(project_id)));
    }

    @GetMapping("/{project_id}/getTasksStats")
    public ResponseEntity<?> getTasksStats(@PathVariable("project_id") Integer project_id){
        return projectService.getTasksStats(project_id);
    }


    @GetMapping("/{project_id}/userStats")
    public ResponseEntity<?> getUserStats(@PathVariable("project_id") Integer project_id){
        return projectService.getUserStats(project_id);
    }

    @GetMapping("/{project_id}/tasksByStatus/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable("project_id") Integer project_id,
        @PathVariable("status" )String status){
        return projectService.getTasksByStatus(project_id,status);
    }

    @PostMapping("/{project_id}/addNotification")
    public ResponseEntity<?> addNotification(@PathVariable("project_id") Integer project_id,
                                             @RequestBody NotificationDTO dto){
        return notificationService.addNotification(project_id,dto);
    }

    @GetMapping("/{project_id}/getNotifications")
    public ResponseEntity<?> getNotifications(@PathVariable("project_id")Integer project_id){

        return notificationService.getNotifications(project_id);
    }

    @GetMapping("/{project_id}/getChangesAll")
    public ResponseEntity<?> getAllEvents(@PathVariable("project_id") Integer project_id){
        return projectService.getAllEvents(project_id);
    }


    @GetMapping("/{project_id}/getMyNotifications")
    public ResponseEntity<?> getMyNotifications(@PathVariable("project_id") Integer project_id){

        return ResponseEntity.ok(modelToDTOConverters.notificationsToDTO( projectNotificationRepository.getNotificationFromUser(Auth.user().getUserId(),project_id)));
    }
    @PostMapping("/{project_id}/setReaded/{notification_id}")
    public ResponseEntity<?> setReaded(@PathVariable("project_id") Integer project_id, @PathVariable("notification_id") Integer notification_id){
        return notificationService.setReaded(project_id,notification_id);
    }

    @GetMapping({
        "{project_id}/getUserPerformance/{user_id}",
        "{project_id}/userStats/{user_id}"
    })
    public ResponseEntity<?> getUserPerformance(@PathVariable("project_id") Integer project_id, @PathVariable("user_id") Integer user_id){
        return userPerformanceService.getUserPerformance(project_id,user_id);
    }



}
