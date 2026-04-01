package com.example._d_task.repositories;

import com.example._d_task.models.FileMetadataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadataModel,Long> {

    @Query("SELECT fmm FROM FileMetadataModel as fmm WHERE fmm.upload_id = :upload_id")
    FileMetadataModel findByUploadId(@Param("upload_id") String upload_id);

    @Query("SELECT f FROM FileMetadataModel f WHERE f.task.task_id = :task_id ORDER BY f.created_at DESC")
    List<FileMetadataModel> findByTaskId(@Param("task_id") Integer task_id);

    @Query("SELECT f FROM FileMetadataModel f WHERE f.file_id = :file_id")
    FileMetadataModel findByFileId(@Param("file_id") Integer file_id);
}
