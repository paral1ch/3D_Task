package com.example._d_task.DTO;

public class UserDTO {
    private Integer user_id;

    private String fullName;
    private String username;
    private String email;


    public UserDTO(){}

    public String getUsername(){
        return this.username;
    }
    public String getUserName(){
        return this.fullName;
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
