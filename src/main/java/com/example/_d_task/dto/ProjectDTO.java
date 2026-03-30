package com.example._d_task.dto;

import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.ProjectModel;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectDTO {
    @Nullable
    private Integer project_id;
    @Nullable
    private String project_name;
    @Nullable
    private String project_description;
    @Nullable
    private UserDTO created_by;
    @Nullable
    private List<String> roles;

    public ProjectDTO(){}
    public ProjectDTO(String project_name, String project_description,Integer project_id,UserDTO user){
        this.project_id=project_id;
        this.project_name=project_name;
        this.project_description=project_description;
        this.created_by=user;
    }

    public ProjectDTO(ProjectModel project){
        this.project_id=project.getProject_id();
        this.project_name=project.getName();
        this.project_description=project.getProjectDescription();
        this.created_by= project.getUser().getUserDTO();
    }

    public ProjectDTO(ProjectModel project, List<ProjectRoles> roles){
        this.project_id=project.getProject_id();
        this.project_name=project.getName();
        this.project_description=project.getProjectDescription();
        this.created_by= project.getUser().getUserDTO();
        this.roles = new ArrayList<>();
        roles.forEach(role -> this.roles.add(role.name()));
    }

    public void setProject_description(String project_description) {
        this.project_description = project_description;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }
    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public String getProject_description() {
        return project_description;
    }

    public String getProject_name() {
        return project_name;
    }


}
