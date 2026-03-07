package com.example._d_task.models;


import jakarta.persistence.*;

@Entity
@Table(name = "sessions")
public class SessionModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer session_id;

    private String email;
    @Column(name = "session_token")

    private String session_token;

    public SessionModel(){}

    public void setSessionToken(String token){this.session_token = token;}
    public void setEmail(String email){this.email = email;}

    public String getEmail(){return this.email;}
    public String getSessionToken(){return this.session_token;}
}
