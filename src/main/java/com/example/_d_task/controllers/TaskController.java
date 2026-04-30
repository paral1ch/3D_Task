package com.example._d_task.controllers;


import com.example._d_task.dto.CommentDTO;
import com.example._d_task.dto.TaskDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskCommentModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.ProjectServices;
import com.example._d_task.services.TaskServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        if(!projectServices.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        if(userProjectRepository.findRolesByUserAndProject(executor.getUserId(),project_id)==null){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user in this project");
        }
        if(executor.getUserId()==null){
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(executor);
        }

        taskServices.setExecutor(task_id,executor.getUserId());

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

        taskServices.setVerifier(task_id,verifier.getUserId());

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

        taskExecutorRepository.deleteExecutor(task_id,executor.getUserId());

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


        taskVerifierRepository.deleteVerifier(task_id,verifier.getUserId());

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

        TaskModel task = taskRepository.findById(task_id);
        task.setStatus(TaskEnum.NEED_REVIEW);
        taskRepository.save(task);

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



        TaskModel task = taskRepository.findById(task_id);
        task.setStatus(taskDTO.getStatus());
        taskRepository.save(task);

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

        if(!ProjectRolePermissions.canModifyProject(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id)) ||
                !ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        TaskModel task = taskRepository.findById(task_id);

        task.setName(dto.getName());
        task.setDeadline(dto.getDeadline());
        task.setDescription(dto.getDescription());
        taskRepository.save(task);

        return ResponseEntity.ok("Task edited");
    }




}
