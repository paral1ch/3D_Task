package com.example._d_task.repositories;

import com.example._d_task.models.TaskExecutorModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository  extends JpaRepository<TaskModel, Long> {

    @Query("SELECT t FROM TaskModel as t WHERE t.task_id = :task_id AND t.project.project_id = :project_id")
    TaskModel findByProjectIdAndTaskID(@Param("task_id") Integer task_id, @Param("project_id") Integer project_id);


    @Query("SELECT t FROM TaskModel as t WHERE t.task_id = :task_id")
    TaskModel findById(@Param("task_id") Integer task_id);

    @Query("SELECT t FROM TaskModel as t WHERE t.parent_task_id = :task_id")
    List<TaskModel> findTaskAsParent(@Param("task_id") Integer task_id);

    @Query("SELECT te FROM TaskExecutorModel as te WHERE :task_id = te.task.task_id AND te.user.user_id = :user_id")
    TaskExecutorModel findExecutorModel(@Param("task_id") Integer task_id, @Param("user_id") Integer user_id);

    @Query("SELECT tf FROM TaskVerifierModel as tf WHERE :task_id = tf.task.task_id AND tf.user.user_id = :user_id")
    TaskExecutorModel findVerifierModel(@Param("task_id") Integer task_id, @Param("user_id") Integer user_id);

    @Query("SELECT tf.user FROM TaskVerifierModel as tf  where :task_id = tf.task.task_id")
    List<UserModel> getVerifiers(@Param("task_id") Integer task_id);

    @Query("SELECT tf.user FROM TaskExecutorModel as tf  where :task_id = tf.task.task_id")
    List<UserModel> getExecutors(@Param("task_id") Integer task_id);

    @Query("SELECT tm FROM TaskModel as tm WHERE tm.task_id in (SELECT te.task.task_id FROM TaskExecutorModel as te WHERE " +
            "te.task.project.project_id= :project_id AND te.user.user_id = :user_id)")
    List<TaskModel> getAsExecutor(@Param("project_id") Integer task_id, @Param("user_id") Integer user_id);

    @Query("SELECT tm FROM TaskModel as tm WHERE tm.task_id in (SELECT te.task.task_id FROM TaskVerifierModel as te WHERE " +
            "te.task.project.project_id = :project_id AND te.user.user_id = :user_id)")
    List<TaskModel> getAsVerifier(@Param("project_id") Integer project_id, @Param("user_id") Integer user_id);
}
