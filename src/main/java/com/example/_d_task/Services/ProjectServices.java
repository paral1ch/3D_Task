package com.example._d_task.Services;


import com.example._d_task.DTO.ProjectDTO;
import com.example._d_task.DTO.UserDTO;
import com.example._d_task.ENUMS.ProjectRoles;
import com.example._d_task.Security.Classes.Auth;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.models.UserModel;
import com.example._d_task.models.UserProjectModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServices {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private UserRepository userRepository;

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
        userProject.setProject_id(project.getProject_id());
        userProject.setUser_id(user.getUserId());
        userProject.setProject_role(ProjectRoles.CREATOR);
        userProjectRepository.save(userProject);
        return userProject;
    }

    public UserProjectModel inviteUserToProject(ProjectModel project, String email){

        UserModel user = userRepository.findByEmail(email);
        UserProjectModel userProject = new UserProjectModel();
        userProject.setProject_id(project.getProject_id());
        userProject.setUser_id(user.getUserId());
        userProject.setProject_role(ProjectRoles.USER);
        userProjectRepository.save(userProject);
        return userProject;
    }

    public ProjectDTO convertModelToDTO(ProjectModel project){

        return new ProjectDTO();
    }

    public void updateRoles(UserDTO user, Integer project_id){
        userProjectRepository.deleteByUserIdAndProjectId(user.getUserId(),project_id);
        user.getRoles().forEach(role ->
                userProjectRepository.save(new UserProjectModel(
                        user.getUserId(),project_id,role
                ))
        );

    }
}
