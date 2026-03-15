package com.example._d_task.controllers;


import com.example._d_task.dto.InviteDTO;
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
import com.example._d_task.services.ProjectServices;
import com.example._d_task.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/project")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private ProjectServices projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    @GetMapping(path = "/{project_id}")
    public ResponseEntity<?> getProject(@PathVariable Integer project_id){
        UserModel user = Auth.user();

        if(!userProjectRepository.getUsersFromProject(project_id).contains(user)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not in this project" +
                    userProjectRepository.getUsersFromProject(project_id).size());
        }

        return ResponseEntity.ok(new ProjectDTO(projectRepository.findByProjectId(project_id)));
    }

    @PostMapping(path = "/create")
    public ResponseEntity<String> createProject(@RequestBody ProjectDTO projectDTO){
        //UserModel user = Auth.user();

        ProjectModel project = projectService.createProjectFromDTO(projectDTO);
        projectService.createUserProjectFrom(project);

        return ResponseEntity.ok(project.getName());
    }

    @PostMapping(path = "/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteDTO inviteDTO){
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
            //return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user " + email);
        }

        if(!projectRepository.existsById(project_id.longValue())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project dont exists");
        }

        ProjectModel project = projectRepository.findByProjectId(project_id);

        projectService.inviteUserToProject(project,email);

        return ResponseEntity.ok("Ok");
    }


    @GetMapping(path = "/myRoles/{project_id}")
    public ResponseEntity<?> myRoles(@PathVariable Integer project_id){
        return ResponseEntity.ok(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id)
                +" " + Auth.user() + " "+  project_id);
    }

    @PostMapping(path = "/{project_id}/edit")
    public ResponseEntity<?> editProject(@PathVariable Integer project_id, @RequestBody ProjectDTO projectDTO){
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

    @GetMapping(path = "/{project_id}/users")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id){
        if(!userProjectRepository.getUsersFromProject(project_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        return ResponseEntity.ok(userService.convertModelsToDTOInProject(
                userProjectRepository.getUsersFromProject(project_id),project_id
        ));
    }

    @GetMapping(path = "/{project_id}/users/edit")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id, UserDTO userDTO){
        if(ProjectRolePermissions.canModifyProject(userProjectRepository.
                findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        UserModel user = userRepository.findByEmail(userDTO.getEmail());


        return ResponseEntity.ok(userService.convertModelsToDTOInProject(
                userProjectRepository.getUsersFromProject(project_id),project_id
        ));
    }

    @PostMapping(path = "/{project_id}/delete")
    public ResponseEntity<?> deleteProject(@PathVariable Integer project_id){
        if (!userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                project_id).contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no right");
        }
        projectRepository.delete(projectRepository.findByProjectId(project_id));
        return ResponseEntity.ok(project_id + " deleted");
    }

    @PostMapping("/{project_id}/deleteUser/{user_id}")
    public ResponseEntity<?> deleteUserFromProject(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){

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
        return ResponseEntity.ok("Deleted user");
    }

    @PostMapping("/{project_id}/addRole/{user_id}")
    public ResponseEntity<?> addRole(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){
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
                userRepository.findById(user_id),
                projectRepository.findByProjectId(project_id),
                ProjectRoles.TASK_CREATOR
        );
        userProjectRepository.save(pr);
        return ResponseEntity.ok("Role added");
    }

    @PostMapping("/{project_id}/deleteRole/{user_id}")
    public ResponseEntity<?> deleteRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
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

}
