package com.example._d_task.repositories;

import com.example._d_task.models.ProjectFilesModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectFilesRepository extends JpaRepository<ProjectFilesModel, Long> {

    @Query("SELECT pf FROM ProjectFilesModel pf WHERE pf.project.project_id = :project_id ORDER BY pf.file_id DESC")
    List<ProjectFilesModel> findByProjectId(@Param("project_id") Integer project_id);

    @Query("SELECT pf FROM ProjectFilesModel pf WHERE pf.file_id = :file_id")
    ProjectFilesModel findByFileId(@Param("file_id") Integer file_id);

    @Query("SELECT pf FROM ProjectFilesModel pf WHERE pf.file_id = :file_id AND pf.project.project_id = :project_id")
    ProjectFilesModel findByFileIdAndProjectId(
        @Param("file_id") Integer file_id,
        @Param("project_id") Integer project_id
    );
}
