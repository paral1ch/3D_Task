package com.example._d_task.services;

import com.example._d_task.dto.CommentDTO;
import com.example._d_task.dto.TaskDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.*;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServices {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskExecutorRepository taskExecutorRepository;
    private final TaskVerifierRepository taskVerifierRepository;
    private final UserProjectRepository userProjectRepository;
    private final ProjectNotificationRepository notificationRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ModelToDTOConverters modelToDTOConverters;
    private final TaskEventService eventService;
    private final PermisssionService permisssionService;
    private final ObjectMapper mapper = new ObjectMapper();

    public TaskServices(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskExecutorRepository taskExecutorRepository,
            TaskVerifierRepository taskVerifierRepository,
            TaskCommentRepository taskCommentRepository,
            UserProjectRepository userProjectRepository,
            ModelToDTOConverters modelToDTOConverters,
            ProjectNotificationRepository notificationRepository,
            TaskEventService eventService,
            PermisssionService permisssionService
    ) {
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskVerifierRepository = taskVerifierRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.modelToDTOConverters = modelToDTOConverters;
        this.notificationRepository = notificationRepository;
        this.eventService = eventService;
        this.permisssionService = permisssionService;
    }

    public void createTask(Integer project_id, TaskDTO taskDTO){
        TaskModel task = new TaskModel();
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());
        task.setParent_task_id(taskDTO.getParent_task_id());
        task.setUser(Auth.user());
        task.setStatus(TaskEnum.IN_PROGRESS);
        task.setProject(projectRepository.findByProjectId(project_id));
        task.setDeadline(taskDTO.getDeadline());
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



    public TaskExecutorModel setExecutor(Integer task_id,Integer user_id){
        TaskExecutorModel executor = new TaskExecutorModel();
        executor.setTask(taskRepository.findById(task_id));
        executor.setUser(userRepository.findByIdNullable(user_id));
        taskExecutorRepository.save(executor);
        return executor;
    }

    public TaskVerifierModel setVerifier(Integer task_id, Integer user_id){
        TaskVerifierModel verifier = new TaskVerifierModel();
        verifier.setTask(taskRepository.findById(task_id));
        verifier.setUser(userRepository.findByIdNullable(user_id));
        taskVerifierRepository.save(verifier);
        return verifier;
    }

    public List<TaskDTO> convertModelsToDTOInTask(List<TaskModel> tasks){
        return tasks.stream().map(
                task -> new TaskDTO(task.getTask_id(),
                        task.getParent_task_id(),
                        task.getProject().getProject_id(),
                        task.getUser().getUserId(),
                        task.getName(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDeadline())
        ).collect(Collectors.toList());
    }


    public ResponseEntity<?> newTask(TaskDTO taskDTO, Integer project_id){
        if(!ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have permission");
        }

        if(taskDTO.getParent_task_id()!=null &&
                taskRepository.findByProjectIdAndTaskID(taskDTO.getParent_task_id(),project_id)==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("There is no such task");
        }
        createTask(project_id,taskDTO);

        return ResponseEntity.ok("Created");
    }

    public ResponseEntity<?> removeTask(Integer project_id, Integer task_id){

        if(!ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have permission");
        }

        deleteTask(task_id);
        return ResponseEntity.ok("Deleted");
    }

    public ResponseEntity<?> getTask(Integer task_id, Integer project_id){
        if(!userProjectRepository.getUsersFromProject(project_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not a part of that project");
        }

        return ResponseEntity.ok(modelToDTOConverters.taskToDTO(
                taskRepository.findById(task_id)
        ));
    }

    public ResponseEntity<?> newExecutor(Integer project_id,
                                         Integer task_id,
                                         UserDTO executor){
        ObjectNode payload = mapper.createObjectNode();
        if(!ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){

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
        nm.setAdressed_to(userRepository.findByIdNullable(executor.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        setExecutor(task_id,executor.getUserId());
        payload.put("executor_id",executor.getUserId());
        try {
            eventService.record(task_id, TaskEventType.EXECUTOR_ASSIGNED,Auth.user().getUserId(), payload,project_id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Executor set" + executor);
    }

    public ResponseEntity<?> newVerifier(Integer project_id,
                                         Integer task_id,
                                         UserDTO verifier){

        if(!ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(verifier.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы были назначены проверяющим задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findByIdNullable(verifier.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        setVerifier(task_id,verifier.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("verifier_id",verifier.getUserId());
        eventService.record(task_id, TaskEventType.VERIFIER_ASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor set" + verifier);
    }

    public ResponseEntity<?> deleteExecutor(Integer project_id,
                                            Integer task_id,
                                            UserDTO executor){

        if(!ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
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
        nm.setAdressed_to(userRepository.findByIdNullable(executor.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);
        taskExecutorRepository.deleteExecutor(task_id,executor.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("executor_id",executor.getUserId());
        eventService.record(task_id, TaskEventType.EXECUTOR_UNASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor deleted" + executor);
    }

    public ResponseEntity<?> deleteVerifier(@PathVariable("project_id") Integer project_id,
                                            @PathVariable("task_id") Integer task_id,
                                            @RequestBody UserDTO verifier){

        if(!ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(verifier.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        ProjectNotificationModel nm = new ProjectNotificationModel();
        nm.setDate(LocalDate.now());
        nm.setProject(projectRepository.findByProjectId(project_id));
        nm.setText("Вы больше не проверяете задачу: " + taskRepository.findById(task_id).getName());
        nm.setAdressed_to(userRepository.findByIdNullable(verifier.getUserId()));
        nm.setUser(Auth.user());
        nm.setReaded(false);
        notificationRepository.save(nm);

        taskVerifierRepository.deleteVerifier(task_id,verifier.getUserId());
        ObjectNode payload = mapper.createObjectNode();
        payload.put("verifier_id",verifier.getUserId());
        eventService.record(task_id, TaskEventType.VERIFIER_UNASSIGNED,Auth.user().getUserId(), payload,project_id);
        return ResponseEntity.ok("Executor deleted" + verifier);
    }

    public ResponseEntity<?> needReview(Integer project_id,
                                        Integer task_id){
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
            nm.setAdressed_to(userRepository.findByIdNullable(verifier.getUser().getUserId()));
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

        return ResponseEntity.ok(modelToDTOConverters.taskToDTO(task));
    }

    public ResponseEntity<?> setStatus(Integer project_id,
                                       Integer task_id,
                                       TaskDTO taskDTO){

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
            nm.setAdressed_to(userRepository.findByIdNullable(verifier.getUser().getUserId()));
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

    public ResponseEntity<?> addComment( Integer task_id,  Integer project_id,
                                         CommentDTO commentDTO){

        if(!permisssionService.canAddComments(task_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        TaskCommentModel comment = new TaskCommentModel();
        comment.setTask(taskRepository.findById(commentDTO.getTask_id()));
        comment.setText(commentDTO.getText());
        comment.setUser(userRepository.findByIdNullable(Auth.user().getUserId()));
        taskCommentRepository.save(comment);

        return ResponseEntity.ok("Comment created" + commentDTO);
    }

    public ResponseEntity<?> editTask(Integer task_id,Integer project_id,  TaskDTO dto){
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

    public ResponseEntity<?> newParent(Integer task_id,
                                       Integer parent_id){
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
