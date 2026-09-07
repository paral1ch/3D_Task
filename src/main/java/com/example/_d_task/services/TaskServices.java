package com.example._d_task.services;

import com.example._d_task.dto.TaskDTO;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.TaskExecutorModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.models.TaskVerifierModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServices {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskExecutorRepository taskExecutorRepository;

    private final TaskVerifierRepository taskVerifierRepository;

    private final UserService userService;

    private final TaskCommentRepository taskCommentRepository;

    private final CommentService commentService;

    private final ProjectServices projectServices;

    public TaskServices(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskExecutorRepository taskExecutorRepository,
            TaskVerifierRepository taskVerifierRepository,
            UserService userService,
            TaskCommentRepository taskCommentRepository,
            CommentService commentService,
            UserProjectRepository userProjectRepository,
            ProjectServices projectServices
    ) {
        this.projectServices = projectServices;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskVerifierRepository = taskVerifierRepository;
        this.userService = userService;
        this.taskCommentRepository = taskCommentRepository;
        this.commentService = commentService;
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
        dto.setVerifiers(userService.convertModelsToDTO(taskRepository.getVerifiers(dto.getTask_id())));
        dto.setExecutors(userService.convertModelsToDTO(taskRepository.getExecutors(dto.getTask_id())));
        dto.setComments(commentService.convertModelsToDTO(taskCommentRepository.getComments(task.getTask_id())));
        return dto;
    }

    public List<TaskDTO> tasksToDTO(List<TaskModel> tasks){
        List<TaskDTO> list = new ArrayList<>();
        for(TaskModel task :tasks){
            list.add(taskToDTO(task));
        }
        return list;
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

    public boolean canAddComments(Integer task_id){
        TaskModel task = taskRepository.findById(task_id);

        if(!taskRepository.getExecutors(task_id).contains(Auth.user()) &&
            !taskRepository.getVerifiers(task_id).contains(Auth.user()) &&
            !projectServices.canModifyTasks(task.getProject().getProject_id())){
            return false;

        }
        return true;
    }
}
