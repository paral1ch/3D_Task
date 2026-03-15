package com.example._d_task.dto;

public class InviteDTO {
    private String email;
    private Integer project_id;


    public InviteDTO(){}

    public Integer getProject_id() {
        return project_id;
    }

    public String getEmail() {
        return email;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
