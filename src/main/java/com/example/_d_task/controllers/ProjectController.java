package com.example._d_task.controllers;


import com.example._d_task.DTO.InviteDTO;
import com.example._d_task.DTO.ProjectDTO;
import com.example._d_task.ENUMS.ProjectRoles;
import com.example._d_task.Security.Classes.Auth;
import com.example._d_task.Services.ProjectServices;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
