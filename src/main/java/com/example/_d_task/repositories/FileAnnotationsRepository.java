package com.example._d_task.repositories;

import com.example._d_task.models.FileAnnotationsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileAnnotationsRepository extends JpaRepository<FileAnnotationsModel, Integer> {

    @Query("SELECT fam FROM FileAnnotationsModel as fam WHERE fam.file.file_id = :file_id")
    public List<FileAnnotationsModel> getAnnotationsByFileId(@Param("file_id") Integer file_id);

    @Query("SELECT fam FROM FileAnnotationsModel as fam WHERE fam.id = :annotation_id")
    FileAnnotationsModel findByAnnotationId(@Param("annotation_id") Integer annotation_id);
}
