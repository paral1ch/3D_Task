package com.example._d_task.controllers;


import com.example._d_task.dto.CommentDTO;
import com.example._d_task.dto.TaskDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.ProjectNotificationModel;
import com.example._d_task.models.TaskCommentModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.models.TaskVerifierModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.ProjectServices;
import com.example._d_task.services.TaskEventService;
import com.example._d_task.services.TaskServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping(path="/project/{project_id}/task")
public class TaskController {
    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private TaskServices taskServices;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectServices projectServices;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private TaskVerifierRepository taskVerifierRepository;

    @Autowired
    private TaskExecutorRepository taskExecutorRepository;

    @Autowired
    private TaskEventService eventService;

    @Autowired
    private ProjectNotificationRepository notificationRepository;

    private ObjectMapper mapper = new ObjectMapper();
    @PostMapping(path = "/create")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO, @PathVariable Integer project_id){
        ;
        if(!ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have permission");
        }

        if(taskDTO.getParent_task_id()!=null &&
        taskRepository.findByProjectIdAndTaskID(taskDTO.getParent_task_id(),project_id)==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("There is no such task");
        }
        taskServices.createTask(project_id,taskDTO);

        return ResponseEntity.ok("Created");
    }

    @PostMapping("/{task_id}/delete")
    public ResponseEntity<?> deleteTask(@PathVariable("project_id") Integer project_id ,
                                        @PathVariable("task_id") Integer task_id){
        if(!ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have permission");
        }

        taskServices.deleteTask(task_id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/{task_id}/get")
    public ResponseEntity<?> getTask(@PathVariable("task_id") Integer task_id,@PathVariable("project_id") Integer project_id){
        if(!userProjectRepository.getUsersFromProject(project_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not a part of that project");
        }

        return ResponseEntity.ok(taskServices.taskToDTO(
                taskRepository.findById(task_id)
        ));
    }

    @PostMapping("/{task_id}/setExecutor")
    public ResponseEntity<?> setExecutor(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO executor){
        ObjectNode payload = mapper.createObjectNode();
        if(!projectServices.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(executor.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        if(executor.getUserId()==null){
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(executor);
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы были назначены исполняющим задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findById(executor.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        taskServices.setExecutor(task_id,executor.getUserId());
        payload.put("executor_id",executor.getUserId());
        try {
            eventService.record(task_id, TaskEventType.EXECUTOR_ASSIGNED,Auth.user().getUserId(), payload,project_id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Executor set" + executor);
    }

    @PostMapping("/{task_id}/setVerifier")
    public ResponseEntity<?> setVerifier(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO verifier){

        if(!projectServices.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(verifier.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы были назначены проверяющим задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findById(verifier.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        taskServices.setVerifier(task_id,verifier.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("verifier_id",verifier.getUserId());
        eventService.record(task_id, TaskEventType.VERIFIER_ASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor set" + verifier);
    }

    @PostMapping("/{task_id}/deleteExecutor")
    public ResponseEntity<?> deleteExecutor(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO executor){

        if(!projectServices.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(executor.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        if(executor.getUserId()==null){
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(executor);
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы больше не исполняете задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findById(executor.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        taskExecutorRepository.deleteExecutor(task_id,executor.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("executor_id",executor.getUserId());
        eventService.record(task_id, TaskEventType.EXECUTOR_UNASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor deleted" + executor);
    }

    @PostMapping("/{task_id}/deleteVerifier")
    public ResponseEntity<?> deleteVerifier(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO verifier){

        if(!projectServices.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(verifier.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы больше не проверяете задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findById(verifier.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);

        taskVerifierRepository.deleteVerifier(task_id,verifier.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("verifier_id",verifier.getUserId());
        eventService.record(task_id, TaskEventType.VERIFIER_UNASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor deleted" + verifier);
    }

    @GetMapping("/{task_id}/getVerifiers")
    public ResponseEntity<?> getVerifiers(@PathVariable("project_id") Integer project_id,
                                          @PathVariable("task_id") Integer task_id){

        return ResponseEntity.ok(taskRepository.getVerifiers(task_id));
    }

    @GetMapping("/{task_id}/getExecutors")
    public ResponseEntity<?> getExecutors(@PathVariable("project_id") Integer project_id,
                                          @PathVariable("task_id") Integer task_id){

        return ResponseEntity.ok(taskRepository.getExecutors(task_id));
    }

    @PostMapping("/{task_id}/needReview")
    public ResponseEntity<?> needReview(@PathVariable("project_id") Integer project_id,
                                        @PathVariable("task_id") Integer task_id){
        if(!taskRepository.getExecutors(task_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        ObjectNode payload = mapper.createObjectNode();

        TaskModel task = taskRepository.findById(task_id);
        payload.put("from",task.getStatus().toString());
        task.setStatus(TaskEnum.NEED_REVIEW);

        for(TaskVerifierModel verifier: task.getTask_verifiers()){
            ProjectNotificationModel nm = new ProjectNotificationModel();
            nm.setDate(LocalDate.now());
            nm.setProject(projectRepository.findByProjectId(project_id));
            nm.setText("Исполнитель поставил статус НУЖНА ПРОВЕРКА задаче: " + taskRepository.findById(task_id).getName());
            nm.setAdressed_to(userRepository.findById(verifier.getUser().getUserId()));
            nm.setUser(Auth.user());
            nm.setReaded(false);
            notificationRepository.save(nm);
        }

        taskRepository.save(task);
        payload.put("to",task.getStatus().toString());
        if (task.getDeadline() != null) {
            payload.put("deadline", task.getDeadline().toString());
        } else {
            payload.putNull("deadline");
        }
        eventService.record(task_id, TaskEventType.STATUS_CHANGED,Auth.user().getUserId(), payload,project_id);

        return ResponseEntity.ok(taskServices.taskToDTO(task));
    }

    @PostMapping("/{task_id}/setStatus")
    public ResponseEntity<?> setStatus(@PathVariable("project_id") Integer project_id,
                                       @PathVariable("task_id") Integer task_id,
                                       @RequestBody TaskDTO taskDTO){



        if(!userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id).contains(ProjectRoles.CREATOR) &&
        taskRepository.findVerifierModel(task_id,Auth.user().getUserId())==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        ObjectNode payload = mapper.createObjectNode();

        TaskModel task = taskRepository.findById(task_id);

        payload.put("from", task.getStatus().toString());
        if(task.getStatus() == TaskEnum.DONE){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden action");
        }
        task.setStatus(taskDTO.getStatus());
        taskRepository.save(task);

        for(TaskVerifierModel verifier: task.getTask_verifiers()){
            ProjectNotificationModel nm = new ProjectNotificationModel();
            nm.setDate(LocalDate.now());
            nm.setProject(projectRepository.findByProjectId(project_id));
            nm.setText("Проверяющий поставил статус " + taskDTO.getStatus().toString()+" задаче: " + taskRepository.findById(task_id).getName());
            nm.setAdressed_to(userRepository.findById(verifier.getUser().getUserId()));
            nm.setUser(Auth.user());
            nm.setReaded(false);
            notificationRepository.save(nm);
        }


        payload.put("to", task.getStatus().toString());
        if (task.getDeadline() != null) {
            payload.put("deadline", task.getDeadline().toString());
        } else {
            payload.putNull("deadline");
        }
        eventService.record(task_id, TaskEventType.STATUS_CHANGED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Status " +taskDTO.getStatus() + " stated");
    }


    @PostMapping("/{task_id}/addComment")
    public ResponseEntity<?> addComment(@PathVariable("task_id") Integer task_id, @PathVariable("project_id") Integer project_id,
                                        @RequestBody CommentDTO commentDTO){

        if(!taskServices.canAddComments(task_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        TaskCommentModel comment = new TaskCommentModel();
        comment.setTask(taskRepository.findById(commentDTO.getTask_id()));
        comment.setText(commentDTO.getText());
        comment.setUser(userRepository.findById(Auth.user().getUserId()));
        taskCommentRepository.save(comment);

        return ResponseEntity.ok("Comment created" + commentDTO);
    }

    @PostMapping("/{task_id}/editTask")
    public ResponseEntity<?> editTask(@PathVariable("task_id") Integer task_id,@PathVariable("project_id") Integer project_id, @RequestBody TaskDTO dto){
        ObjectNode payload = mapper.createObjectNode();
        if(!ProjectRolePermissions.canModifyProject(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id)) ||
                !ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        TaskModel task = taskRepository.findById(task_id);
        if(!task.getDeadline().equals(dto.getDeadline())){
            payload.put("from",task.getDeadline().toString());
            payload.put("to",dto.getDeadline().toString());
            eventService.record(task_id, TaskEventType.DEADLINE_CHANGED, Auth.user().getUserId(), payload,project_id);
        }

        payload.removeAll();
        task.setName(dto.getName());
        task.setDeadline(dto.getDeadline());
        task.setDescription(dto.getDescription());
        taskRepository.save(task);

        payload.put("task_id", task_id);
        eventService.record(task_id, TaskEventType.EDITED, Auth.user().getUserId(), payload, project_id);

        return ResponseEntity.ok("Task edited");
    }

    @PostMapping("/{task_id}/{new_parent_task_id}")
    public ResponseEntity<?> newParent(@PathVariable("task_id") Integer task_id,
                                       @PathVariable("new_parent_task_id") Integer parent_id){
        if(task_id.equals(parent_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden action");
        }

        TaskModel task = taskRepository.findById(task_id);
        TaskModel parent_task = taskRepository.findById(parent_id);


        if(task == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("there is no such task");
        }
        ObjectNode payload = mapper.createObjectNode();
        payload.put("from", task.getParent_task_id());
        Boolean isCreatorOrTaskCreatorOfParent = false;
        if(parent_task!=null){
            isCreatorOrTaskCreatorOfParent = ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),parent_task.getProject().getProject_id()));
        }
        else{
            isCreatorOrTaskCreatorOfParent = true;
        }

        Boolean isCreatorOrTaskCreatorOfTask = ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),task.getProject().getProject_id()));

        if(!isCreatorOrTaskCreatorOfParent){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if(!isCreatorOrTaskCreatorOfTask){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if(parent_task != null){
            task.setParent_task_id(parent_task.getTask_id());
        }
        else{
            task.setParent_task_id(null);
        }
        taskRepository.save(task);
        payload.put("to",task.getParent_task_id());
        eventService.record(task_id, TaskEventType.PARENT_CHANGED,Auth.user().getUserId(), payload,task.getProject().getProject_id());
        return ResponseEntity.ok("task changed");
    }


}
