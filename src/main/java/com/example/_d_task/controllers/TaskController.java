package com.example._d_task.controllers;


import com.example._d_task.dto.CommentDTO;
import com.example._d_task.dto.TaskDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.services.TaskServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path="/project/{project_id}/task")
public class TaskController {

    private final TaskServices taskServices;

    private final TaskRepository taskRepository;

    public TaskController( TaskServices taskServices,
                          TaskRepository taskRepository){
        this.taskServices = taskServices;
        this.taskRepository = taskRepository;
    }

    private ObjectMapper mapper = new ObjectMapper();
    @PostMapping(path = "/create")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO, @PathVariable Integer project_id){
        return taskServices.newTask(taskDTO,project_id);
    }

    @PostMapping("/{task_id}/delete")
    public ResponseEntity<?> deleteTask(@PathVariable("project_id") Integer project_id ,
                                        @PathVariable("task_id") Integer task_id){
        return taskServices.removeTask(project_id,task_id);
    }

    @GetMapping("/{task_id}/get")
    public ResponseEntity<?> getTask(@PathVariable("task_id") Integer task_id,@PathVariable("project_id") Integer project_id){
        return taskServices.getTask(task_id,project_id);
    }

    @PostMapping("/{task_id}/setExecutor")
    public ResponseEntity<?> setExecutor(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO executor){
        return taskServices.newExecutor(project_id,task_id,executor);
    }

    @PostMapping("/{task_id}/setVerifier")
    public ResponseEntity<?> setVerifier(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO verifier){
        return taskServices.newVerifier(project_id,task_id,verifier);
    }

    @PostMapping("/{task_id}/deleteExecutor")
    public ResponseEntity<?> deleteExecutor(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO executor){

        return taskServices.deleteExecutor(project_id,task_id,executor);
    }

    @PostMapping("/{task_id}/deleteVerifier")
    public ResponseEntity<?> deleteVerifier(@PathVariable("project_id") Integer project_id,
                                         @PathVariable("task_id") Integer task_id,
                                         @RequestBody UserDTO verifier){

        return taskServices.deleteVerifier(project_id,task_id,verifier);
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
        return taskServices.needReview(project_id,task_id);
    }

    @PostMapping("/{task_id}/setStatus")
    public ResponseEntity<?> setStatus(@PathVariable("project_id") Integer project_id,
                                       @PathVariable("task_id") Integer task_id,
                                       @RequestBody TaskDTO taskDTO){
        return taskServices.setStatus(project_id,task_id,taskDTO);
    }


    @PostMapping("/{task_id}/addComment")
    public ResponseEntity<?> addComment(@PathVariable("task_id") Integer task_id, @PathVariable("project_id") Integer project_id,
                                        @RequestBody CommentDTO commentDTO){
        return taskServices.addComment(task_id,project_id,commentDTO);
    }

    @PostMapping("/{task_id}/editTask")
    public ResponseEntity<?> editTask(@PathVariable("task_id") Integer task_id,@PathVariable("project_id") Integer project_id, @RequestBody TaskDTO dto){

        return taskServices.editTask(task_id,project_id,dto);
    }

    @PostMapping("/{task_id}/{new_parent_task_id}")
    public ResponseEntity<?> newParent(@PathVariable("task_id") Integer task_id,
                                       @PathVariable("new_parent_task_id") Integer parent_id){
        return taskServices.newParent(task_id,parent_id);
    }


}
