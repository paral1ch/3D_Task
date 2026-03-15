package com.example._d_task.dto;

import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.UserModel;

import java.util.List;

public class UserDTO {
    private Integer user_id;

    private String fullName;
    private String username;
    private String email;
    private List<ProjectRoles> roles;

    public UserDTO(){}

    public UserDTO(String username, String email, String fullName,Integer user_id){
        this.email=email;
        this.username = username;
        this.fullName = fullName;
        this.user_id = user_id;
    }

    public UserDTO(UserModel user){
        this.email=user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.user_id = user.getUserId();
    }

    public UserDTO(UserModel user,List<ProjectRoles> roles){
        this.email=user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.user_id = user.getUserId();
        this.roles = roles;
    }

    public String getUsername(){
        return this.username;
    }
    public String getFullname(){
        return this.fullName;
    }

    public List<ProjectRoles> getRoles() {
        return roles;
    }

    public void setRoles(List<ProjectRoles> roles) {
        this.roles = roles;
    }

    public String getEmail(){
        return this.email;
    }

    public Integer getUserId(){
        return this.user_id;
    }
    public void setDisplay_name(String display_name) {
        this.username = display_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.fullName = name;
    }

}
