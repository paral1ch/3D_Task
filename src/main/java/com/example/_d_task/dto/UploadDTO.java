package com.example._d_task.dto;

import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class UploadDTO {
    @Nullable
    private String fileName;

    @Nullable
    private String contentType;

    @Nullable
    private Integer taskId;
    @Nullable
    private String key;

    @Nullable
    private Integer asset_id;

    @Nullable
    private Integer version_id;
}
