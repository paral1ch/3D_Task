package com.example._d_task.models;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class UserModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer user_id;

    private String user_name;
    private String display_name;
    private String email;
    private String pass_hash;

    public UserModel(){}

    public String getDisplayName(){
        return this.display_name;
    }
    public String getUserName(){
        return this.user_name;
    }

    public String getEmail(){
        return this.email;
    }

    public Integer getUserId(){
        return this.user_id;
    }
    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.user_name = name;
    }

    public void setPassHash(String passHash) {
        this.pass_hash = passHash;
    }


}
