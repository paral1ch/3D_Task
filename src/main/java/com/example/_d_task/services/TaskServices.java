package com.example._d_task.services;

import com.example._d_task.dto.TaskDTO;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskExecutorModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.models.TaskVerifierModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServices {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskExecutorRepository taskExecutorRepository;

    private final TaskVerifierRepository taskVerifierRepository;


    private final TaskCommentRepository taskCommentRepository;


    public TaskServices(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskExecutorRepository taskExecutorRepository,
            TaskVerifierRepository taskVerifierRepository,
            TaskCommentRepository taskCommentRepository

    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskVerifierRepository = taskVerifierRepository;
        this.taskCommentRepository = taskCommentRepository;
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


}
