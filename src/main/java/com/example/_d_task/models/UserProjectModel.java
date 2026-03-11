package com.example._d_task.models;


import com.example._d_task.ENUMS.ProjectRoles;
import com.example._d_task.models.Id.UserProjectId;
import jakarta.persistence.*;

@Entity
@IdClass(UserProjectId.class)
@Table(name="project_user")
public class UserProjectModel {
    @Id
    @Column(name = "user_id")
    private Integer user_id;

    @Id
    @Column(name = "project_id")
    private Integer project_id;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectRoles project_role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "user_id",insertable = false,updatable = false)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",referencedColumnName = "project_id",insertable = false,updatable = false)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectModel project;



    public UserProjectModel(){}

    public UserProjectModel(Integer user_id,Integer project_id,ProjectRoles role){
        this.project_role=role;
        this.project_id = project_id;
        this.user_id=user_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setProject_role(ProjectRoles project_role) {
        this.project_role = project_role;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public UserModel getUser() {
        return user;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public ProjectModel getProject() {
        return project;
    }

    public ProjectRoles getProject_role() {
        return project_role;
    }
}
