package com.example._d_task.dto;

import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Date;

@Data
public class SessionDTO {
    @Nullable
    private String refreshToken;
    @Nullable
    private String accessToken;
    private Date refresh_exceeds_at;
    private Date access_exceeds_at;

    public SessionDTO(){}

}
