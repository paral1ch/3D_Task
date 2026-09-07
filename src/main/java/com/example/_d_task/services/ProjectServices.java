package com.example._d_task.services;


import com.example._d_task.dto.ProjectDTO;
import com.example._d_task.dto.UserDTO;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.models.UserModel;
import com.example._d_task.models.UserProjectModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import com.example._d_task.security.Classes.Auth;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServices {

    private final ProjectRepository projectRepository;

    private final UserProjectRepository userProjectRepository;

    private final UserRepository userRepository;

    public ProjectServices(ProjectRepository projectRepository, UserProjectRepository userProjectRepository,
                           UserRepository userRepository){
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userProjectRepository = userProjectRepository;
    }

    public boolean canModifyTasks(Integer project_id){

        return ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id)
        );
    }


    public ProjectModel createProjectFromDTO(ProjectDTO projectDTO){
        UserModel user = Auth.user();
        ProjectModel project = new ProjectModel();
        project.setUser_id(user.getUserId());
        project.setProjectDescription(projectDTO.getProject_description());
        project.setName(projectDTO.getProject_name());

        projectRepository.save(project);
        return project;
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

    public ProjectDTO convertModelToDTO(ProjectModel project){

        return new ProjectDTO();
    }

    public List<ProjectDTO> convertModelsToDTOInProject(List<ProjectModel> projects){
        return projects.stream().map(
                project -> new ProjectDTO(project,userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project.getProject_id()))
        ).collect(Collectors.toList());
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


}
