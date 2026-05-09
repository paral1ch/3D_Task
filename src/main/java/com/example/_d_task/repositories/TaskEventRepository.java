package com.example._d_task.repositories;

import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.TaskEventModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskEventRepository extends JpaRepository<TaskEventModel, Long> {

    @Query("SELECT tem FROM TaskEventModel as tem where tem.project.project_id = :project_id")
    List<TaskEventModel> getAllEvents(@Param("project_id") Integer project_id);

    @Query("SELECT tem FROM TaskEventModel as tem where tem.event_type = :event_type")
    List<TaskEventModel> getAllEventsWithType(@Param("event_type")TaskEventType event_type);

    @Query("SELECT tem FROM TaskEventModel as tem where tem.event_type in :event_type")
    List<TaskEventModel> getAllEventsWithType(@Param("event_type")List<TaskEventType> event_type);
}
