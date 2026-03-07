package com.example._d_task.models;

import com.example._d_task.ENUMS.Role;
import jakarta.persistence.*;

@Entity
@Table(name="users")
public class UserModel {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer user_id;
    @Column(name = "full_name")
    private String fullName;
    @Column(name="username")
    private String username;
    private String email;
    @Column(name = "pass_hash")
    private String passHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public UserModel(){}

    public String getUsername(){
        return this.username;
    }
    public String getFullName(){
        return this.fullName;
    }

    public String getEmail(){
        return this.email;
    }
    public String getPashHash(){return this.passHash;}
    public Integer getUserId(){
        return this.user_id;
    }
    public Role getRole(){return this.role;}

    public void setUsername(String display_name) {
        this.username = display_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public void setRole(Role role){this.role = role;}
}
