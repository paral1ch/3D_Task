package com.example._d_task.DTO;

public class ProjectDTO {
    private Integer project_id;
    private String project_name;
    private String project_description;


    public ProjectDTO(String project_name, String project_description){
        this.project_name=project_name;
        this.project_description=project_description;

    }

    public void setProject_description(String project_description) {
        this.project_description = project_description;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_description() {
        return project_description;
    }

    public String getProject_name() {
        return project_name;
    }
}
