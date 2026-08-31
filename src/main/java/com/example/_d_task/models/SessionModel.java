package com.example._d_task.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "sessions")
@Data
public class SessionModel {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer session_id;

    private String refresh_token;
    private String access_token;
    private Date refresh_exceeds_at;
    private Date access_exceeds_at;

    public SessionModel(){}
}
