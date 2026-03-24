package com.example._d_task.repositories;

import com.example._d_task.models.FileMetadataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileMetadataRepository extends JpaRepository<FileMetadataModel,Long> {

    @Query("SELECT fmm FROM FileMetadataModel as fmm WHERE fmm.upload_id = :upload_id")
    FileMetadataModel findByUploadId(@Param("upload_id") String upload_id);

}
