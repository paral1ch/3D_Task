package com.example._d_task.controllers;


import com.example._d_task.dto.*;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.*;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.*;
import com.example._d_task.services.servicesUtils.ModelToDTOConverters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping(path="/project")
public class ProjectController {

    private final ProjectRepository projectRepository;

    private final UserProjectRepository userProjectRepository;

    private final ProjectServices projectService;

    private final UserRepository userRepository;

    private final UserService userService;

    private final TaskServices taskServices;

    private final TaskRepository taskRepository;

    private final ProjectNotificationRepository projectNotificationRepository;

    private final NotificationService notificationService;

    private final TaskEventRepository eventRepository;

    private final TaskEventService eventService;
    private final ModelToDTOConverters modelToDTOConverters;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Logger log = Logger.getLogger(ProjectController.class.getName());

    public ProjectController(ProjectRepository projectRepository,UserProjectRepository userProjectRepository,ProjectServices projectService,UserRepository userRepository,UserService userService
                            ,TaskServices taskServices,TaskRepository taskRepository,ProjectNotificationRepository projectNotificationRepository,
                             NotificationService notificationService,TaskEventRepository eventRepository,TaskEventService eventService,
                             ModelToDTOConverters modelToDTOConverters, ModelToDTOConverters converter){
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.taskServices = taskServices;
        this.taskRepository = taskRepository;
        this.projectNotificationRepository = projectNotificationRepository;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.userService = userService;
        this.modelToDTOConverters = modelToDTOConverters;
    }

    @GetMapping(path = "/myProjects")
    public ResponseEntity<?> getProjects(){
        UserModel user = Auth.user();
        //log.info(projectService.convertModelsToDTOInProject(userProjectRepository.getProjectsFromUser(user.getUserId())).toString());
        return ResponseEntity.ok(modelToDTOConverters.projectsToDTO(userProjectRepository.getUserProjects(user.getUserId())));
    }


    @GetMapping(path = "/{project_id}")
    public ResponseEntity<?> getProject(@PathVariable Integer project_id){
        return projectService.getProject(project_id);
    }


    @PostMapping(path = "/create")
    public ResponseEntity<String> createProject(@RequestBody ProjectDTO projectDTO){

        return projectService.createProject(projectDTO);
    }

    @PostMapping(path = "/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteDTO inviteDTO){
        return projectService.inviteToProject(inviteDTO);
    }


    @GetMapping(path = "/myRoles/{project_id}")
    public ResponseEntity<?> myRoles(@PathVariable Integer project_id){
        return ResponseEntity.ok(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id)
                +" " + Auth.user() + " "+  project_id);
    }

    @PostMapping(path = "/{project_id}/edit")
    public ResponseEntity<?> editProject(@PathVariable Integer project_id, @RequestBody ProjectDTO projectDTO){
        return projectService.editProject(project_id,projectDTO);
    }

    @GetMapping(path = "/{project_id}/users")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id){
        return projectService.getProjectUsers(project_id);
    }
    // Ваще не помню че за эндпоинт и почему у него такой странный путь, потом вспомнить и перелопатить\удалить
    //@GetMapping(path = "/{project_id}/users/edit")
    //public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id, UserDTO userDTO){
    //    if(ProjectRolePermissions.canModifyProject(userProjectRepository.
    //            findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
    //        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
    //    }
    //    return ResponseEntity.ok(userService.convertModelsToDTOInProject(
    //            userProjectRepository.getUsersFromProject(project_id),project_id
    //    ));
    //}

    @PostMapping(path = "/{project_id}/delete")
    public ResponseEntity<?> deleteProject(@PathVariable Integer project_id){
        return projectService.deleteProject(project_id);
    }

    @PostMapping("/{project_id}/deleteUser/{user_id}")
    public ResponseEntity<?> deleteUserFromProject(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){
        return projectService.deleteUserFromProject(project_id,user_id);
    }

    @PostMapping("/{project_id}/addRole/{user_id}")
    public ResponseEntity<?> addRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
        return projectService.addRole(project_id,user_id);
    }

    @PostMapping("/{project_id}/deleteRole/{user_id}")
    public ResponseEntity<?> deleteRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
        return projectService.deleteRole(project_id,user_id);
    }

    @GetMapping("/{project_id}/getTasks")
    public ResponseEntity<?> getTasks(@PathVariable("project_id") Integer project_id){
        if(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id) ==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cant get tasks from that project");
        }

        return ResponseEntity.ok(modelToDTOConverters.tasksToDTO(projectRepository.findTasks(project_id)));
    }

    @GetMapping("/{project_id}/getTasksStats")
    public ResponseEntity<?> getTasksStats(@PathVariable("project_id") Integer project_id){
        return projectService.getTasksStats(project_id);
    }


    @GetMapping("/{project_id}/userStats")
    public ResponseEntity<?> getUserStats(@PathVariable("project_id") Integer project_id){
        return projectService.getUserStats(project_id);
    }

    @GetMapping("/{project_id}/tasksByStatus/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable("project_id") Integer project_id,
        @PathVariable("status" )String status){
        return projectService.getTasksByStatus(project_id,status);
    }

    @PostMapping("/{project_id}/addNotification")
    public ResponseEntity<?> addNotification(@PathVariable("project_id") Integer project_id,
                                             @RequestBody NotificationDTO dto){
        return notificationService.addNotification(project_id,dto);
    }

    @GetMapping("/{project_id}/getNotifications")
    public ResponseEntity<?> getNotifications(@PathVariable("project_id")Integer project_id){
        //List<ProjectNotificationModel> notList = (List<ProjectNotificationModel>) projectRepository.findByProjectId(project_id).getNotifications();
        List<ProjectNotificationModel> notList = projectNotificationRepository.getFromProject(project_id);

        List<NotificationDTO> list = modelToDTOConverters.notificationsToDTO(notList);
        list.removeIf(dto -> dto.getAdressed_to() != null);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{project_id}/getChangesAll")
    public ResponseEntity<?> getAllEvents(@PathVariable("project_id") Integer project_id){
        List<ProjectRoles> roles =
            userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                project_id
            );
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }

        List<TaskEventModel> list = eventRepository.getAllEvents(project_id);
        return ResponseEntity.ok(modelToDTOConverters.eventsToDTO(list));
    }


    @GetMapping("/{project_id}/getMyNotifications")
    public ResponseEntity<?> getMyNotifications(@PathVariable("project_id") Integer project_id){

        return ResponseEntity.ok(modelToDTOConverters.notificationsToDTO( projectNotificationRepository.getNotificationFromUser(Auth.user().getUserId(),project_id)));
    }
    @PostMapping("/{project_id}/setReaded/{notification_id}")
    public ResponseEntity<?> setReaded(@PathVariable("project_id") Integer project_id, @PathVariable("notification_id") Integer notification_id){
        if(!projectNotificationRepository.getNotificationById(notification_id).getAdressed_to().equals(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden action");
        }
        ProjectNotificationModel pnm = projectNotificationRepository.getNotificationById(notification_id);
        pnm.setReaded(true);
        projectNotificationRepository.save(pnm);

        return ResponseEntity.ok("Readed");

    }

    @GetMapping({
        "{project_id}/getUserPerformance/{user_id}",
        "{project_id}/userStats/{user_id}"
    })
    public ResponseEntity<?> getUserPerformance(@PathVariable("project_id") Integer project_id, @PathVariable("user_id") Integer user_id){
        UserModel current = Auth.user();
        List<ProjectRoles> currentRoles = userProjectRepository.findRolesByUserAndProject(current.getUserId(), project_id);
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in this project");
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

        Map<String, Integer> executingByStatus = new LinkedHashMap<>();
        Map<String, Integer> verifyingByStatus = new LinkedHashMap<>();
        for (TaskEnum taskStatus : TaskEnum.values()) {
            executingByStatus.put(taskStatus.name(), 0);
            verifyingByStatus.put(taskStatus.name(), 0);
        }

        for (TaskModel task : executingTasks) {
            String statusName = task.getStatus().name();
            executingByStatus.put(statusName, executingByStatus.get(statusName) + 1);
            if (TaskEnum.DONE.equals(task.getStatus())) {
                executingDone++;
            }
        }

        for (TaskModel task : verifyingTasks) {
            String statusName = task.getStatus().name();
            verifyingByStatus.put(statusName, verifyingByStatus.get(statusName) + 1);
            if (TaskEnum.DONE.equals(task.getStatus())) {
                verifyingDone++;
            }
        }

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
                LocalDate fromDeadline = readPayloadDate(event.getPayload(), "from");
                LocalDate toDeadline = readPayloadDate(event.getPayload(), "to");
                if (!latestKnownDeadlineByTask.containsKey(eventTaskId)) {
                    latestKnownDeadlineByTask.put(eventTaskId, fromDeadline);
                }
                latestKnownDeadlineByTask.put(eventTaskId, toDeadline);
                continue;
            }

            if (TaskEventType.EXECUTOR_ASSIGNED.equals(event.getEvent_type()) && executingTaskIds.contains(eventTaskId)) {
                Integer executorId = readPayloadInt(event.getPayload(), "executor_id");
                if (executorId != null && executorId.equals(user_id)) {
                    assignedAtByTask.put(eventTaskId, event.getCreated_at());
                }
                continue;
            }

            if (TaskEventType.STATUS_CHANGED.equals(event.getEvent_type())) {
                String toStatus = readPayloadString(event.getPayload(), "to");
                if (TaskEnum.DONE.name().equalsIgnoreCase(toStatus)) {
                    doneAtByTask.put(eventTaskId, event.getCreated_at());
                    LocalDate deadlineFromStatusEvent = readPayloadDate(event.getPayload(), "deadline");
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
        result.put("executing_by_status", executingByStatus);
        result.put("verifying_by_status", verifyingByStatus);
        result.put("executor_completion_durations", executionDurations);
        result.put("executor_completion_avg_minutes", durationCount == 0 ? null : Math.round((totalDurationMinutes * 1.0 / durationCount) * 100.0) / 100.0);

        return ResponseEntity.ok(result);
    }

    private JsonNode normalizePayloadNode(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        if (!payload.isTextual()) {
            return payload;
        }
        String raw = payload.asText();
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer readPayloadInt(JsonNode payload, String key) {
        JsonNode normalized = normalizePayloadNode(payload);
        if (normalized == null || !normalized.isObject()) {
            return null;
        }
        JsonNode value = normalized.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.canConvertToInt()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText().trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String readPayloadString(JsonNode payload, String key) {
        JsonNode normalized = normalizePayloadNode(payload);
        if (normalized == null || !normalized.isObject()) {
            return null;
        }
        JsonNode value = normalized.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private LocalDate readPayloadDate(JsonNode payload, String key) {
        String value = readPayloadString(payload, key);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception ignored) {
            return null;
        }
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
