package com.example._d_task.controllers;


import com.example._d_task.dto.*;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.enums.TaskEnum;
import com.example._d_task.enums.TaskEventType;
import com.example._d_task.models.*;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private ProjectServices projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskServices taskServices;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectNotificationRepository projectNotificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskEventRepository eventRepository;

    @Autowired
    private TaskEventService eventService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Logger log = Logger.getLogger(ProjectController.class.getName());

    @GetMapping(path = "/myProjects")
    public ResponseEntity<?> getProjects(){
        UserModel user = Auth.user();

        //log.info(projectService.convertModelsToDTOInProject(userProjectRepository.getProjectsFromUser(user.getUserId())).toString());


        return ResponseEntity.ok(projectService.convertModelsToDTOInProject(userProjectRepository.getUserProjects(user.getUserId())));
    }


    @GetMapping(path = "/{project_id}")
    public ResponseEntity<?> getProject(@PathVariable Integer project_id){
        UserModel user = Auth.user();

        if(!userProjectRepository.getUsersFromProject(project_id).contains(user)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not in this project" +
                    userProjectRepository.getUsersFromProject(project_id).size());
        }

        return ResponseEntity.ok(new ProjectDTO(projectRepository.findByProjectId(project_id),userProjectRepository.findRolesByUserAndProject(user.getUserId(),project_id)));
    }


    @PostMapping(path = "/create")
    public ResponseEntity<String> createProject(@RequestBody ProjectDTO projectDTO){
        //UserModel user = Auth.user();

        ProjectModel project = projectService.createProjectFromDTO(projectDTO);
        projectService.createUserProjectFrom(project);

        return ResponseEntity.ok("Сделано");
    }

    @PostMapping(path = "/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteDTO inviteDTO){
        String email = inviteDTO.getEmail();
        Integer project_id = inviteDTO.getProject_id();

        if(!userProjectRepository
                .findRolesByUserAndProject(
                        Auth.user().getUserId(), project_id)
                .contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights" + project_id);
        }

        if(userRepository.findByEmail(email)==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user " + email);
            //return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such user " + email);
        }

        if(!projectRepository.existsById(project_id.longValue())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project dont exists");
        }

        if(!userProjectRepository.getUserProjects(inviteDTO.getEmail(),inviteDTO.getProject_id()).isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already in project" + userProjectRepository.getUserProjects(inviteDTO.getEmail(),inviteDTO.getProject_id()).isEmpty());
        }

        ProjectModel project = projectRepository.findByProjectId(project_id);

        projectService.inviteUserToProject(project,email);

        return ResponseEntity.ok("Ok");
    }


    @GetMapping(path = "/myRoles/{project_id}")
    public ResponseEntity<?> myRoles(@PathVariable Integer project_id){
        return ResponseEntity.ok(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),project_id)
                +" " + Auth.user() + " "+  project_id);
    }

    @PostMapping(path = "/{project_id}/edit")
    public ResponseEntity<?> editProject(@PathVariable Integer project_id, @RequestBody ProjectDTO projectDTO){
        UserModel user = Auth.user();

        if(!userProjectRepository
                .findRolesByUserAndProject(
                        Auth.user().getUserId(), project_id)
                .contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        ProjectModel project = projectRepository.findByProjectId(project_id);

        project.setName(projectDTO.getProject_name());
        project.setProjectDescription(projectDTO.getProject_description());
        projectRepository.save(project);

        return ResponseEntity.ok("Data saved");
    }

    @GetMapping(path = "/{project_id}/users")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id){
        if(!userProjectRepository.getUsersFromProject(project_id).contains(Auth.user())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        return ResponseEntity.ok(userService.convertModelsToDTOInProject(
                userProjectRepository.getUsersFromProject(project_id),project_id
        ));
    }

    @GetMapping(path = "/{project_id}/users/edit")
    public ResponseEntity<?> getProjectUsers(@PathVariable Integer project_id, UserDTO userDTO){
        if(ProjectRolePermissions.canModifyProject(userProjectRepository.
                findRolesByUserAndProject(Auth.user().getUserId(), project_id))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        UserModel user = userRepository.findByEmail(userDTO.getEmail());


        return ResponseEntity.ok(userService.convertModelsToDTOInProject(
                userProjectRepository.getUsersFromProject(project_id),project_id
        ));
    }

    @PostMapping(path = "/{project_id}/delete")
    public ResponseEntity<?> deleteProject(@PathVariable Integer project_id){
        if (!userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                project_id).contains(ProjectRoles.CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no right");
        }
        projectRepository.delete(projectRepository.findByProjectId(project_id));
        return ResponseEntity.ok(project_id + " deleted");
    }

    @PostMapping("/{project_id}/deleteUser/{user_id}")
    public ResponseEntity<?> deleteUserFromProject(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){

        if(Auth.user().getUserId()==user_id){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cant delete yourself");
        }
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                project_id);
        if(!ProjectRolePermissions.canModifyProject(roles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        userProjectRepository.deleteByUserIdAndProjectId(
                user_id,project_id
        );
        userProjectRepository.deleteAll(userProjectRepository.getUserProjects(user_id,project_id));
        return ResponseEntity.ok("Deleted user");
    }

    @PostMapping("/{project_id}/addRole/{user_id}")
    public ResponseEntity<?> addRole(@PathVariable("project_id") Integer project_id,
                                                   @PathVariable("user_id") Integer user_id){
        if(!ProjectRolePermissions.canModifyProject(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project_id)
        )){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        if(userProjectRepository.findRolesByUserAndProject(user_id,project_id).contains(ProjectRoles.TASK_CREATOR)){
           return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Role is already assigned to that user");
        }
        UserProjectModel pr = new UserProjectModel(
                userRepository.findByIdNullable(user_id),
                projectRepository.findByProjectId(project_id),
                ProjectRoles.TASK_CREATOR
        );
        userProjectRepository.save(pr);
        return ResponseEntity.ok("Role added");
    }

    @PostMapping("/{project_id}/deleteRole/{user_id}")
    public ResponseEntity<?> deleteRole(@PathVariable("project_id") Integer project_id,
                                     @PathVariable("user_id") Integer user_id){
        if(!ProjectRolePermissions.canModifyProject(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        project_id)
        )){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        if(!userProjectRepository.findRolesByUserAndProject(user_id,project_id).contains(ProjectRoles.TASK_CREATOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("That user dont have role");
        }
        userProjectRepository.deleteByUserIdAndProjectIdAndRole(user_id,project_id,ProjectRoles.TASK_CREATOR);
        return ResponseEntity.ok("Role deleted");
    }

    @GetMapping("/{project_id}/getTasks")
    public ResponseEntity<?> getTasks(@PathVariable("project_id") Integer project_id){
        if(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id) ==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cant get tasks from that project");
        }

        return ResponseEntity.ok(taskServices.tasksToDTO(projectRepository.findTasks(project_id)));
    }

    @GetMapping("/{project_id}/getTasksStats")
    public ResponseEntity<?> getTasksStats(@PathVariable("project_id") Integer project_id){
        HashMap<String, Integer> hashMap = new HashMap<>();

        for(TaskEnum status: TaskEnum.values()){
            hashMap.put(status.name(),0);
        };
        hashMap.put("STATUS_SUM",0);
        hashMap.put("OUTDATED",0);
        List<TaskModel> tasks = projectRepository.findTasks(project_id);
        for(TaskModel task: tasks) {
            hashMap.put(task.getStatus().name(), hashMap.get(task.getStatus().name())+1);
            //if(!task.getDeadline().isAfter(LocalDate.now())){
                //    hashMap.put("OUTDATED",hashMap.get("OUTDATED")+1);
                //}
            hashMap.put("STATUS_SUM", hashMap.get("STATUS_SUM")+1);
        }
        return ResponseEntity.ok(hashMap);
    }

    @GetMapping("/{project_id}/userStats")
    public ResponseEntity<?> getUserStats(@PathVariable("project_id") Integer project_id){
        List<UserModel> users = userProjectRepository.getUsersFromProject(project_id);
        List<UserStatDTO> stats = new ArrayList<>();
        for(UserModel user: users){
            UserStatDTO stat = new UserStatDTO();
            stat.setUser(user.getUserDTO());
            stat.setExecuting(taskServices.tasksToDTO(taskRepository.getAsExecutor(project_id,user.getUserId())));
            stat.setVerifying(taskServices.tasksToDTO(taskRepository.getAsVerifier(project_id,user.getUserId())));
            stats.add(stat);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{project_id}/tasksByStatus/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable("project_id") Integer project_id,
        @PathVariable("status" )String status){


        List<TaskModel> tasks = projectRepository.findTasks(project_id);
        List<TaskDTO> tasksDTO = new ArrayList<>();

        for(TaskModel task: tasks){
            if(task.getStatus().name().equals(status)){
                tasksDTO.add(taskServices.taskToDTO(task));
            }

        }

        return ResponseEntity.ok(tasksDTO);
    }

    @PostMapping("/{project_id}/addNotification")
    public ResponseEntity<?> addNotification(@PathVariable("project_id") Integer project_id,
                                             @RequestBody NotificationDTO dto){
        if (!projectService.canModifyTasks(project_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        ProjectNotificationModel projectNotification = new ProjectNotificationModel();
        projectNotification.setText(dto.getText());
        projectNotification.setProject(projectRepository.findByProjectId(project_id));
        projectNotification.setUser(Auth.user());
        projectNotification.setDate(dto.getDate());
        projectNotificationRepository.save(projectNotification);

        return ResponseEntity.ok("Notification created");
    }

    @GetMapping("/{project_id}/getNotifications")
    public ResponseEntity<?> getNotifications(@PathVariable("project_id")Integer project_id){
        //List<ProjectNotificationModel> notList = (List<ProjectNotificationModel>) projectRepository.findByProjectId(project_id).getNotifications();
        List<ProjectNotificationModel> notList = projectNotificationRepository.getFromProject(project_id);

        List<NotificationDTO> list = notificationService.convertModelsToDTO(notList);
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
        return ResponseEntity.ok(eventService.modelsToDTO(list));
    }


    @GetMapping("/{project_id}/getMyNotifications")
    public ResponseEntity<?> getMyNotifications(@PathVariable("project_id") Integer project_id){

        return ResponseEntity.ok(notificationService.convertModelsToDTO( projectNotificationRepository.getNotificationFromUser(Auth.user().getUserId(),project_id)));
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
        result.put("user", userService.convertModelToDTO(user));
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
