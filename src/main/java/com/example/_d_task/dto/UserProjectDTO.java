package com.example._d_task.dto;

import com.example._d_task.enums.ProjectRoles;

import java.util.List;

public class UserProjectDTO {
    private Integer user_id;
    private List<ProjectRoles> roles;
    private Integer project_id;

    public UserProjectDTO(){}

    public UserProjectDTO(Integer project_id, Integer user_id, List<ProjectRoles> roles){
        this.project_id = project_id;
        this.user_id = user_id;
        this.roles = roles;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public void setRoles(List<ProjectRoles> roles) {
        this.roles = roles;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public List<ProjectRoles> getRoles() {
        return roles;
    }

    public Integer getUser_id() {
        return user_id;
    }
}
