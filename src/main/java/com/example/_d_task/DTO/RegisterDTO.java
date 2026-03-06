package com.example._d_task.DTO;

public class RegisterDTO {

    private String user_name;
    private String display_name;
    private String email;
    private String pass_hash;

    public RegisterDTO(String user_name, String display_name, String email,String passwordHash){
        this.display_name=display_name;
        this.user_name=user_name;
        this.pass_hash=passwordHash;
        this.email=email;
    }
    public String getDisplayName(){
        return this.display_name;
    }
    public String getUserName(){
        return this.user_name;
    }

    public String getEmail(){
        return this.email;
    }
    public String getPasswordHash(){return this.pass_hash;}
    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.user_name = name;
    }

    public void setPasswordHash(String passwordHash){this.pass_hash=passwordHash;}
}
