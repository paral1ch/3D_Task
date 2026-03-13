package com.example._d_task.models;


import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "projects")
public class ProjectModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer project_id;

    @Column(name = "project_name")
    private String project_name;

    @Column(name = "project_description")
    private String project_description;

    @Column(name = "user_id")
    private Integer user_id;

    @ManyToOne
    @JoinColumn(name ="user_id", referencedColumnName = "user_id",insertable = false,updatable = false)
    private UserModel user;

    @OneToMany(mappedBy = "project",cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserProjectModel> userProjectModel;

    public ProjectModel(){}


    public void setName(String name) {
        this.project_name = name;
    }

    public void setUser_id(Integer id) {
        this.user_id = id;
    }

    public void setProjectDescription(String description) {
        this.project_description = description;
    }

    public Integer getUser_id() {
        return this.user_id;
    }

    public UserModel getCreated_by() {
        return user;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public String getProjectDescription() {
        return project_description;
    }

    public String getName() {
        return project_name;
    }

    public UserModel getUser() {
        return user;
    }

    public Collection<UserProjectModel> getUserProjectModel() {
        return userProjectModel;
    }
}
