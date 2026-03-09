package com.example._d_task.repositories;

import com.example._d_task.models.ProjectModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
    
    @Query("SELECT pm FROM ProjectModel as pm WHERE :project_id=pm.project_id")
    ProjectModel findByProjectId(@Param("project_id") Integer project_id);
}