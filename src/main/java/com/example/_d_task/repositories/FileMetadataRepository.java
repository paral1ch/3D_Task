package com.example._d_task.repositories;

import com.example._d_task.models.FileMetadataModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Query("SELECT f FROM FileMetadataModel f WHERE f.s3key = :s3Key")
    FileMetadataModel findByFileS3Key(@Param("s3Key") String s3Key);

    @Query("Select count(fmm.asset_id) FROM FileMetadataModel as fmm where fmm.asset_id = :asset_id")
    Integer assetCount(@Param("asset_id") Integer asset_id);

    @Query("SELECT f FROM FileMetadataModel f WHERE f.asset_id = :asset_id")
    List<FileMetadataModel> findByAssetId(@Param("asset_id") Integer asset_id);

    @Query(
        value = "SELECT nextval('file_metadata_asset_id_seq'::regclass)",
        nativeQuery = true
    )
    Integer nextAssetId();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FileMetadataModel f WHERE f.asset_id = :asset_id ORDER BY f.version DESC")
    List<FileMetadataModel> findByAssetIdForUpdate(@Param("asset_id") Integer asset_id);

    @Query("SELECT f FROM FileMetadataModel f WHERE f.asset_id = :asset_id ORDER BY f.version DESC, f.created_at DESC")
    List<FileMetadataModel> findVersionsByAssetId(@Param("asset_id") Integer asset_id);
}
