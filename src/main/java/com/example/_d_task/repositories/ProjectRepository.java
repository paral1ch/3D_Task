package com.example._d_task.repositories;

import com.example._d_task.models.ProjectModel;
import com.example._d_task.models.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
    
    @Query("SELECT pm FROM ProjectModel as pm WHERE :project_id=pm.project_id")
    ProjectModel findByProjectId(@Param("project_id") Integer project_id);

    @Query("SELECT tm FROM TaskModel as tm WHERE :project_id IN (SELECT pm.project_id FROM ProjectModel as pm WHERE :project_id = pm.project_id)")
    List<TaskModel> findTasks(@Param("project_id") Integer project_id);
}