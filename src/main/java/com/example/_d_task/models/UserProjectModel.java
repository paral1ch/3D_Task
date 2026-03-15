package com.example._d_task.models;


import com.example._d_task.enums.ProjectRoles;
import jakarta.persistence.*;

@Entity
@Table(name="project_user")
public class UserProjectModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer project_user_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectRoles project_role;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectModel project;



    public UserProjectModel(){}

    public UserProjectModel(UserModel user_id,ProjectModel project_id,ProjectRoles role){
        this.project_role=role;
        this.project = project_id;
        this.user=user_id;
    }

    public Integer getProject_user_id() {
        return project_user_id;
    }

    //public void setProject_id(Integer project_id) {
    //    this.project_id = project_id;
    //}
//
    //public void setUser_id(Integer user_id) {
    //    this.user_id = user_id;
    //}

    public void setProject_role(ProjectRoles project_role) {
        this.project_role = project_role;
    }

    //public Integer getProject_id() {
    //    return project_id;
    //}

    public UserModel getUser() {
        return user;
    }

    //public Integer getUser_id() {
    //    return user_id;
    //}


    public void setUser(UserModel user) {
        this.user = user;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }

    public ProjectModel getProject() {
        return project;
    }

    public ProjectRoles getProject_role() {
        return project_role;
    }
}
