package com.example._d_task.services;

import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.models.TaskEventModel;
import com.example._d_task.models.TaskModel;
import com.example._d_task.models.UserModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.servicesUtils.JSONPayloadUtils;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserPerformanceService {
    private final UserProjectRepository userProjectRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskEventRepository eventRepository;
    private  final ModelToDTOConverters modelToDTOConverters;
    private final JSONPayloadUtils jsonPayloadUtils;

    public UserPerformanceService(UserProjectRepository userProjectRepository,
    UserRepository userRepository,
    ProjectRepository projectRepository,
    TaskRepository taskRepository,
    TaskEventRepository eventRepository,
    ModelToDTOConverters modelToDTOConverters,
    JSONPayloadUtils jsonPayloadUtils){
        this.userRepository = userRepository;
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.modelToDTOConverters = modelToDTOConverters;
        this.jsonPayloadUtils = jsonPayloadUtils;
    }

    public ResponseEntity<?> getUserPerformance(Integer project_id, Integer user_id){
        UserModel current = Auth.user();
        List<ProjectRoles> currentRoles = userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id);
        if (currentRoles == null || currentRoles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        UserModel user = userRepository.findByIdNullable(user_id);
        ProjectModel project = projectRepository.findByProjectId(project_id);
        if (user == null || project == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden action");
        }
        List<ProjectRoles> targetRoles = userProjectRepository.findRolesByUserAndProject(user_id, project_id);
        if (targetRoles == null || targetRoles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in this project " + targetRoles);
        }

        List<TaskModel> executingTasks = taskRepository.getAsExecutor(project_id, user_id);
        List<TaskModel> verifyingTasks = taskRepository.getAsVerifier(project_id, user_id);
        int executingCount = executingTasks.size();
        int verifyingCount = verifyingTasks.size();

        int executingDone = 0;
        int verifyingDone = 0;
        int executingOverdue = 0;
        int verifyingOverdue = 0;
        LocalDate now = LocalDate.now();
        List<TaskEventModel> projectEvents = eventRepository.getAllEvents(project_id);
        projectEvents.sort(Comparator.comparing(TaskEventModel::getCreated_at, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Integer, LocalDateTime> assignedAtByTask = new HashMap<>();
        Map<Integer, LocalDateTime> doneAtByTask = new HashMap<>();
        Map<Integer, LocalDate> deadlineAtDoneByTask = new HashMap<>();
        Map<Integer, LocalDate> latestKnownDeadlineByTask = new HashMap<>();
        Set<Integer> executingTaskIds = new HashSet<>();
        Set<Integer> relevantTaskIds = new HashSet<>();
        for (TaskModel task : executingTasks) {
            executingTaskIds.add(task.getTask_id());
            relevantTaskIds.add(task.getTask_id());
        }
        for (TaskModel task : verifyingTasks) {
            relevantTaskIds.add(task.getTask_id());
        }

        for (TaskEventModel event : projectEvents) {
            if (event == null || event.getTask() == null || event.getTask().getTask_id() == null) {
                continue;
            }
            Integer eventTaskId = event.getTask().getTask_id();
            if (!relevantTaskIds.contains(eventTaskId)) {
                continue;
            }
            if (event.getCreated_at() == null) {
                continue;
            }

            if (TaskEventType.DEADLINE_CHANGED.equals(event.getEvent_type())) {
                LocalDate fromDeadline = jsonPayloadUtils.readPayloadDate(event.getPayload(), "from");
                LocalDate toDeadline = jsonPayloadUtils.readPayloadDate(event.getPayload(), "to");
                if (!latestKnownDeadlineByTask.containsKey(eventTaskId)) {
                    latestKnownDeadlineByTask.put(eventTaskId, fromDeadline);
                }
                latestKnownDeadlineByTask.put(eventTaskId, toDeadline);
                continue;
            }

            if (TaskEventType.EXECUTOR_ASSIGNED.equals(event.getEvent_type()) && executingTaskIds.contains(eventTaskId)) {
                Integer executorId = jsonPayloadUtils.readPayloadInt(event.getPayload(), "executor_id");
                if (executorId != null && executorId.equals(user_id)) {
                    assignedAtByTask.put(eventTaskId, event.getCreated_at());
                }
                continue;
            }

            if (TaskEventType.STATUS_CHANGED.equals(event.getEvent_type())) {
                String toStatus = jsonPayloadUtils.readPayloadString(event.getPayload(), "to");
                if (TaskEnum.DONE.name().equalsIgnoreCase(toStatus)) {
                    doneAtByTask.put(eventTaskId, event.getCreated_at());
                    LocalDate deadlineFromStatusEvent = jsonPayloadUtils.readPayloadDate(event.getPayload(), "deadline");
                    LocalDate deadlineAtDone = deadlineFromStatusEvent != null
                            ? deadlineFromStatusEvent
                            : latestKnownDeadlineByTask.get(eventTaskId);
                    if (deadlineAtDone == null && event.getTask() != null) {
                        deadlineAtDone = event.getTask().getDeadline();
                    }
                    deadlineAtDoneByTask.put(eventTaskId, deadlineAtDone);
                }
            }
        }

        for (TaskModel task : executingTasks) {
            if (isTaskOverdue(task, now, doneAtByTask, deadlineAtDoneByTask)) {
                executingOverdue++;
            }
        }

        for (TaskModel task : verifyingTasks) {
            if (isTaskOverdue(task, now, doneAtByTask, deadlineAtDoneByTask)) {
                verifyingOverdue++;
            }
        }

        List<Map<String, Object>> executionDurations = new ArrayList<>();
        long totalDurationMinutes = 0L;
        int durationCount = 0;

        for (TaskModel task : executingTasks) {
            if (!TaskEnum.DONE.equals(task.getStatus())) {
                continue;
            }
            Integer taskId = task.getTask_id();
            LocalDateTime assignedAt = assignedAtByTask.get(taskId);
            LocalDateTime doneAt = doneAtByTask.get(taskId);
            if (doneAt == null && task.getDone_at() != null) {
                doneAt = task.getDone_at().atStartOfDay();
            }
            if (assignedAt == null || doneAt == null || doneAt.isBefore(assignedAt)) {
                continue;
            }

            long minutes = Duration.between(assignedAt, doneAt).toMinutes();
            totalDurationMinutes += minutes;
            durationCount++;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("task_id", taskId);
            row.put("task_name", task.getName());
            row.put("assigned_at", assignedAt);
            row.put("done_at", doneAt);
            row.put("duration_minutes", minutes);
            row.put("duration_hours", Math.round((minutes / 60.0) * 100.0) / 100.0);
            executionDurations.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", modelToDTOConverters.userToDTO(user));
        result.put("project_id", project_id);
        result.put("roles", targetRoles);
        result.put("executing_count", executingCount);
        result.put("verifying_count", verifyingCount);
        result.put("executing_done", executingDone);
        result.put("verifying_done", verifyingDone);
        result.put("executing_overdue", executingOverdue);
        result.put("verifying_overdue", verifyingOverdue);
        result.put("executing_by_status", getExecutingByStatus(project_id,user_id));
        result.put("verifying_by_status", getVerifyingByStatus(project_id,user_id));
        result.put("executor_completion_durations", executionDurations);
        result.put("executor_completion_avg_minutes", durationCount == 0 ? null : Math.round((totalDurationMinutes * 1.0 / durationCount) * 100.0) / 100.0);

        return ResponseEntity.ok(result);
    }

    public Map<String,Integer> getExecutingByStatus(Integer project_id, Integer user_id){
        List<TaskModel> executingTasks = taskRepository.getAsExecutor(project_id, user_id);
        int executingDone = 0;

        Map<String, Integer> executingByStatus = new LinkedHashMap<>();

        for (TaskEnum taskStatus : TaskEnum.values()) {
            executingByStatus.put(taskStatus.name(), 0);
        }

        for (TaskModel task : executingTasks) {
            String statusName = task.getStatus().name();
            executingByStatus.put(statusName, executingByStatus.get(statusName) + 1);
            if (TaskEnum.DONE.equals(task.getStatus())) {
                executingDone++;
            }
        }

        return executingByStatus;
    }

    public Map<String,Integer> getVerifyingByStatus(Integer project_id, Integer user_id){
        List<TaskModel> executingTasks = taskRepository.getAsExecutor(project_id, user_id);
        List<TaskModel> verifyingTasks = taskRepository.getAsVerifier(project_id, user_id);

        int verifyingCount = verifyingTasks.size();
        int verifyingDone = 0;

        Map<String, Integer> verifyingByStatus = new LinkedHashMap<>();
        for (TaskEnum taskStatus : TaskEnum.values()) {
            verifyingByStatus.put(taskStatus.name(), 0);
        }

        for (TaskModel task : verifyingTasks) {
            String statusName = task.getStatus().name();
            verifyingByStatus.put(statusName, verifyingByStatus.get(statusName) + 1);
            if (TaskEnum.DONE.equals(task.getStatus())) {
                verifyingDone++;
            }
        }

        return verifyingByStatus;
    }

    private boolean isTaskOverdue(
            TaskModel task,
            LocalDate now,
            Map<Integer, LocalDateTime> doneAtByTask,
            Map<Integer, LocalDate> deadlineAtDoneByTask
    ) {
        if (task == null) {
            return false;
        }
        LocalDate deadline = task.getDeadline();
        Integer taskId = task.getTask_id();

        if (TaskEnum.DONE.equals(task.getStatus())) {
            LocalDateTime doneAt = doneAtByTask.get(taskId);
            if (doneAt == null && task.getDone_at() != null) {
                doneAt = task.getDone_at().atStartOfDay();
            }
            LocalDate deadlineAtDone = deadlineAtDoneByTask.get(taskId);
            if (deadlineAtDone != null) {
                deadline = deadlineAtDone;
            }
            if (deadline == null || doneAt == null) {
                return false;
            }
            return doneAt.toLocalDate().isAfter(deadline);
        }

        return deadline != null && deadline.isBefore(now);
    }
}
