package com.example._d_task.services;


import com.example._d_task.dto.*;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.models.*;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ProjectServices {
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final UserRepository userRepository;
    private final TaskEventRepository taskEventRepository;
    private final TaskRepository taskRepository;
    private final ModelToDTOConverters modelToDTOConverters;

    public ProjectServices(ProjectRepository projectRepository, UserProjectRepository userProjectRepository,
                           UserRepository userRepository,
                           TaskRepository taskRepository, ModelToDTOConverters modelToDTOConverters,
                           TaskEventRepository taskEventRepository){
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userProjectRepository = userProjectRepository;
        this.modelToDTOConverters = modelToDTOConverters;
        this.taskRepository = taskRepository;
        this.taskEventRepository = taskEventRepository;
    }

    public boolean canModifyTasks(Integer project_id){

        return ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id)
        );
    }




    public UserProjectModel createUserProjectFrom(ProjectModel project){
        UserModel user = Auth.user();
        UserProjectModel userProject = new UserProjectModel();
        userProject.setProject(project);
        userProject.setUser(user);
        userProject.setProject_role(ProjectRoles.CREATOR);
        userProjectRepository.save(userProject);
        return userProject;
    }

    public UserProjectModel inviteUserToProject(ProjectModel project, String email){

        UserModel user = userRepository.findByEmail(email);
        UserProjectModel userProject = new UserProjectModel();
        userProject.setProject(project);
        userProject.setUser(user);
        userProject.setProject_role(ProjectRoles.USER);
        userProjectRepository.save(userProject);
        return userProject;
    }

    public void updateRoles(UserDTO user, Integer project_id){
        userProjectRepository.deleteByUserIdAndProjectId(user.getUserId(),project_id);
        user.getRoles().forEach(role ->
                userProjectRepository.save(new UserProjectModel(
                        userRepository.findByEmail(user.getEmail()),projectRepository.findByProjectId(project_id),role
                ))
        );
    }

    public boolean userHasAccess(Integer user_id, Integer project_id){
        return userProjectRepository.findRolesByUserAndProject(user_id,project_id).isEmpty();
    }

    public ResponseEntity<?> getProject(Integer project_id){
        UserModel user = Auth.user();
        if(!userProjectRepository.getUsersFromProject(project_id).contains(user)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not in this project" +
                    userProjectRepository.getUsersFromProject(project_id).size());
        }
        return ResponseEntity.ok(new ProjectDTO(projectRepository.findByProjectId(project_id),userProjectRepository.findRolesByUserAndProject(user.getUserId(),project_id)));
    }

    public ResponseEntity<String> createProject(ProjectDTO dto){
        //UserModel user = Auth.user();
        ProjectModel project = modelToDTOConverters.DTOToProject(dto);
        createUserProjectFrom(project);
        return ResponseEntity.ok("Сделано");
    }

    public ResponseEntity<?> inviteToProject(InviteDTO inviteDTO){
        String email = inviteDTO.getEmail();
        Integer project_id = inviteDTO.getProject_id();

        if(!userProjectRepository
                .findRolesByUserAndProject(
                        Auth.user().getUserId(), project_id)
                .contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights" + project_id);
        }

        if(userRepository.findByEmail(email)==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user " + email);
        }

        if(!projectRepository.existsById(project_id.longValue())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project dont exists");
        }

        if(!userProjectRepository.getUserProjects(inviteDTO.getEmail(),inviteDTO.getProject_id()).isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already in project" + userProjectRepository.getUserProjects(inviteDTO.getEmail(),inviteDTO.getProject_id()).isEmpty());
        }

        ProjectModel project = projectRepository.findByProjectId(project_id);

        inviteUserToProject(project,email);
        return ResponseEntity.ok("Ok");
    }

    public ResponseEntity<?> editProject(Integer project_id, ProjectDTO projectDTO){
        UserModel user = Auth.user();

        if(!userProjectRepository
                .findRolesByUserAndProject(
                        Auth.user().getUserId(), project_id)
                .contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        ProjectModel project = projectRepository.findByProjectId(project_id);

        project.setName(projectDTO.getProject_name());
        project.setProjectDescription(projectDTO.getProject_description());
        projectRepository.save(project);
        return ResponseEntity.ok("Data saved");
    }

    public ResponseEntity<?> getProjectUsers(Integer project_id){
        if(!userProjectRepository.getUsersFromProject(project_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        return ResponseEntity.ok(modelToDTOConverters.usersInProjectToDTO(
                userProjectRepository.getUsersFromProject(project_id),project_id
        ));
    }

    public ResponseEntity<String> deleteProject(Integer project_id){
        if (!userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                project_id).contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no right");
        }
        projectRepository.delete(projectRepository.findByProjectId(project_id));
        return ResponseEntity.ok(project_id + " deleted");
    }

    public ResponseEntity<?> deleteUserFromProject(Integer project_id, Integer user_id){

        if(Auth.user().getUserId()==user_id){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cant delete yourself");
        }
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                project_id);
        if(!ProjectRolePermissions.canModifyProject(roles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        userProjectRepository.deleteByUserIdAndProjectId(
                user_id,project_id
        );
        userProjectRepository.deleteAll(userProjectRepository.getUserProjects(user_id,project_id));
        return ResponseEntity.ok("Deleted user");
    }

    public ResponseEntity<?> addRole(Integer project_id, Integer user_id){
        if(!ProjectRolePermissions.canModifyProject(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project_id)
        )){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        if(userProjectRepository.findRolesByUserAndProject(user_id,project_id).contains(ProjectRoles.TASK_CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Role is already assigned to that user");
        }
        UserProjectModel pr = new UserProjectModel(
                userRepository.findByIdNullable(user_id),
                projectRepository.findByProjectId(project_id),
                ProjectRoles.TASK_CREATOR
        );
        userProjectRepository.save(pr);
        return ResponseEntity.ok("Role added");
    }

    public ResponseEntity<?> deleteRole(Integer project_id,
                                        Integer user_id){
        if(!ProjectRolePermissions.canModifyProject(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project_id)
        )){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        if(!userProjectRepository.findRolesByUserAndProject(user_id,project_id).contains(ProjectRoles.TASK_CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("That user dont have role");
        }
        userProjectRepository.deleteByUserIdAndProjectIdAndRole(user_id,project_id,ProjectRoles.TASK_CREATOR);
        return ResponseEntity.ok("Role deleted");
    }


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //Добавить сюда валидацию принадлежнасти юзера к проекту.
    public ResponseEntity<?> getTasksStats(Integer project_id){
        HashMap<String, Integer> hashMap = new HashMap<>();

        for(TaskEnum status: TaskEnum.values()){
            hashMap.put(status.name(),0);
        };
        hashMap.put("STATUS_SUM",0);
        hashMap.put("OUTDATED",0);
        List<TaskModel> tasks = projectRepository.findTasks(project_id);
        for(TaskModel task: tasks) {
            hashMap.put(task.getStatus().name(), hashMap.get(task.getStatus().name())+1);
            //if(!task.getDeadline().isAfter(LocalDate.now())){
            //    hashMap.put("OUTDATED",hashMap.get("OUTDATED")+1);
            //}
            hashMap.put("STATUS_SUM", hashMap.get("STATUS_SUM")+1);
        }
        return ResponseEntity.ok(hashMap);
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //Сюда тоже обавить сюда валидацию принадлежнасти юзера к проекту.
    public ResponseEntity<?> getUserStats(Integer project_id){
        List<UserModel> users = userProjectRepository.getUsersFromProject(project_id);
        List<UserStatDTO> stats = new ArrayList<>();
        for(UserModel user: users){
            UserStatDTO stat = new UserStatDTO();
            stat.setUser(user.getUserDTO());
            stat.setExecuting(modelToDTOConverters.tasksToDTO(taskRepository.getAsExecutor(project_id,user.getUserId())));
            stat.setVerifying(modelToDTOConverters.tasksToDTO(taskRepository.getAsVerifier(project_id,user.getUserId())));
            stats.add(stat);
        }
        return ResponseEntity.ok(stats);
    }

    public ResponseEntity<?> getTasksByStatus(Integer project_id,
                                              String status){
        List<TaskModel> tasks = projectRepository.findTasks(project_id);
        List<TaskDTO> tasksDTO = new ArrayList<>();
        for(TaskModel task: tasks){
            if(task.getStatus().name().equals(status)){
                tasksDTO.add(modelToDTOConverters.taskToDTO(task));
            }
        }
        return ResponseEntity.ok(tasksDTO);
    }

    public ResponseEntity<?> getAllEvents( Integer project_id){
        List<ProjectRoles> roles =
                userProjectRepository.findRolesByUserAndProject(
                        Auth.user().getUserId(),
                        project_id
                );
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        List<TaskEventModel> list = taskEventRepository.getAllEvents(project_id);
        return ResponseEntity.ok(modelToDTOConverters.eventsToDTO(list));
    }


}
