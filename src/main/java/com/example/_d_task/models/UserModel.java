package com.example._d_task.models;

import com.example._d_task.ENUMS.Role;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="users")
public class UserModel implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.passHash;
    }

    @Override
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
