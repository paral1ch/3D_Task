package com.example._d_task.DTO;

public class RegisterDTO {

    private String fullName;
    private String username;
    private String email;
    private String passHash;

    public RegisterDTO(String user_name, String display_name, String email,String passwordHash){
        this.username=display_name;
        this.fullName=user_name;
        this.passHash=passwordHash;
        this.email=email;
    }
    public String getUsername(){
        return this.username;
    }
    public String getFullName(){
        return this.fullName;
    }

    public String getEmail(){
        return this.email;
    }
    public String getPasswordHash(){return this.passHash;}
    public void setUsername(String display_name) {
        this.username = display_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullname(String name) {
        this.fullName = name;
    }

    public void setPasswordHash(String passwordHash){this.passHash=passwordHash;}
}
