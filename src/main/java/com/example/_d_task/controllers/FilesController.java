package com.example._d_task.controllers;


import com.amazonaws.services.s3.model.PartETag;
import com.example._d_task.dto.FileDTO;
import com.example._d_task.dto.UploadDTO;
import com.example._d_task.dto.UploadedPartDTO;
import com.example._d_task.enums.FileStatus;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.FileMetadataModel;
import com.example._d_task.models.ProjectFilesModel;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.repositories.*;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.MultipartService;
import com.example._d_task.services.TaskServices;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FilesController {

    private MultipartService multipartService;

    private FileMetadataRepository fileMetadataRepository;

    private ProjectFilesRepository projectFilesRepository;

    private TaskRepository taskRepository;

    private UserProjectRepository userProjectRepository;

    private ProjectRepository projectRepository;

    private TaskServices taskServices;

    private FileAnnotationsRepository fileAnnotationsRepository;

    private final Integer URL_LIFESPAN = 15;

    public FilesController(
            MultipartService multipartService,
            FileMetadataRepository fileMetadataRepository,
            ProjectFilesRepository projectFilesRepository,
            TaskRepository taskRepository,
            UserProjectRepository userProjectRepository,
            ProjectRepository projectRepository,
            TaskServices taskServices,
            FileAnnotationsRepository fileAnnotationsRepository
    ){
        this.fileMetadataRepository = fileMetadataRepository;
        this.multipartService = multipartService;
        this.projectFilesRepository = projectFilesRepository;
        this.userProjectRepository = userProjectRepository;
        this.taskRepository = taskRepository;
        this.taskServices = taskServices;
        this.fileAnnotationsRepository = fileAnnotationsRepository;
        this.projectRepository = projectRepository;
    }


    private static final Logger log = Logger.getLogger(
            FilesController.class.getName()
    );

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<?> initiateUpload(
            @RequestBody UploadDTO uploadDTO){
        ProjectModel pm = projectRepository.findByTaskId(uploadDTO.getTaskId());

        if(pm==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no such project");
        }
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                pm.getProject_id()
        );
        Boolean isCreator = roles != null && ProjectRolePermissions.canCreateTask(roles);
        Boolean isVerifierOrExecutor = taskRepository.getExecutors(uploadDTO.getTaskId()).contains(Auth.user()) ||
                taskRepository.getVerifiers(uploadDTO.getTaskId()).contains(Auth.user());
        if(!isCreator && !isVerifierOrExecutor)
        {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights " + isCreator + " " + isVerifierOrExecutor);
        }

        String key = String.format("tasks/%d/%s_%s", uploadDTO
                .getTaskId(), UUID.randomUUID(),uploadDTO.getFileName());
        String uploadId= multipartService.initiateMultipartUpload(key, uploadDTO.getContentType());
        FileMetadataModel file = new FileMetadataModel();
        file.setUpload_id(uploadId);
        file.setStatus(FileStatus.PENDING);
        file.setTask(taskRepository.findById(uploadDTO.getTaskId()));
        file.setS3key(key);
        file.setFile_name(uploadDTO.getFileName());
        file.setVerifier_file(taskRepository.getVerifiers(uploadDTO.getTaskId()).contains(Auth.user()));
        file.setUser(Auth.user());
        Integer requestedAssetId = uploadDTO.getAsset_id();
        if(requestedAssetId != null){
            List<FileMetadataModel> assetFiles = fileMetadataRepository.findByAssetIdForUpdate(requestedAssetId);
            if(assetFiles == null || assetFiles.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("asset not found");
            }
            Integer assetTaskId = assetFiles.get(0).getTask().getTask_id();
            if(!Objects.equals(assetTaskId, uploadDTO.getTaskId())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("asset belongs to another task");
            }
            int lastVersion = assetFiles.stream()
                    .map(FileMetadataModel::getVersion)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);
            file.setAsset_id(requestedAssetId);
            file.setVersion(lastVersion + 1);
        } else {
            Integer newAssetId = fileMetadataRepository.nextAssetId();
            file.setAsset_id(newAssetId);
            file.setVersion(1);
        }

        fileMetadataRepository.save(file);
        return ResponseEntity.ok(new InitiateUploadResponse(uploadId,key,file.getFile_id()));
    }

    @GetMapping("/upload/{upload_id}/parts/{part_number}")
    public ResponseEntity<?> getPresignedURL(@PathVariable("upload_id") String upload_id,
                                             @PathVariable("part_number") Integer part_number){

        FileMetadataModel file = fileMetadataRepository.findByUploadId(upload_id);

        if(file==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error");
        }
        if(!taskServices.canAddComments(file.getTask().getTask_id())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }



        String url = multipartService.generatePresignedUrlForPart(file.getS3key(),upload_id,part_number,URL_LIFESPAN);

        return ResponseEntity.ok(url);
    }

    @PostMapping("/uploads/{uploadId}/complete")
    public ResponseEntity<String> completeUpload(
            @PathVariable String uploadId,
            @RequestBody CompleteUploadRequest request) {
        FileMetadataModel file = fileMetadataRepository.findByUploadId(uploadId);
        if(file==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error");
        }
        if(!taskServices.canAddComments(file.getTask().getTask_id())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }
        List<PartETag> etags = request.getParts().stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getEtag()))
                .collect(Collectors.toList());

        multipartService.completeMultipartUpload(file.getS3key(), uploadId, etags);

        FileMetadataModel metadata = fileMetadataRepository.findByUploadId(uploadId);
        metadata.setStatus(FileStatus.COMPLETED);
        fileMetadataRepository.save(metadata);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTaskFiles(@PathVariable Integer taskId) {
        ProjectModel pm = projectRepository.findByTaskId(taskId);

        if (pm == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such project");
        }

        if (userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), pm.getProject_id()) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        List<FileDTO> files = fileMetadataRepository.findByTaskId(taskId).stream().map(file -> {
            FileDTO dto = new FileDTO();
            dto.setFile_id(file.getFile_id());
            dto.setFile_name(file.getFile_name());
            dto.setTask_id(file.getTask().getTask_id());
            dto.setStatus(file.getStatus());
            dto.setCreated_at(file.getCreated_at());
            dto.setUpdated_at(file.getUpdated_at());
            dto.setUpload_id(file.getUpload_id());
            dto.setS3Key(file.getS3key());
            log.info(dto.getS3Key());
            dto.setVerifier_file(taskRepository.getVerifiers(taskId).contains(file.getUser()));
            dto.setUser(file.getUser().getUserDTO());
            dto.setVersion(file.getVersion());
            dto.setAsset_id(file.getAsset_id());
            return dto;
        }).toList();

        return ResponseEntity.ok(files);
    }

    @PostMapping("/project/{project_id}/upload")
    public ResponseEntity<?> initiateProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @RequestBody UploadDTO uploadDTO
    ) {
        ProjectModel project = projectRepository.findByProjectId(project_id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such project");
        }

        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                project_id
        );
        if (roles == null || !ProjectRolePermissions.canCreateTask(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }

        if (uploadDTO == null || uploadDTO.getFileName() == null || uploadDTO.getFileName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fileName is required");
        }

        String contentType = uploadDTO.getContentType() == null || uploadDTO.getContentType().isBlank()
                ? "application/octet-stream"
                : uploadDTO.getContentType();
        String fileName = uploadDTO.getFileName().trim();
        String key = String.format(
                "projects/%d/%s_%s",
                project_id,
                UUID.randomUUID(),
                fileName
        );
        String uploadId = multipartService.initiateMultipartUpload(key, contentType);
        return ResponseEntity.ok(new InitiateUploadResponse(uploadId, key, null));
    }

    @GetMapping("/project/{project_id}/upload/{upload_id}/parts/{part_number}")
    public ResponseEntity<?> getProjectPresignedPartUrl(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("upload_id") String upload_id,
            @PathVariable("part_number") Integer part_number,
            @RequestParam("key") String key
    ) {
        if (!canUploadProjectFiles(project_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if (!isProjectFileKeyValid(project_id, key)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid key");
        }

        String url = multipartService.generatePresignedUrlForPart(
                key,
                upload_id,
                part_number,
                URL_LIFESPAN
        );
        return ResponseEntity.ok(url);
    }

    @GetMapping("/project/{project_id}/uploads/{uploadId}/parts")
    public ResponseEntity<?> getProjectUploadedParts(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("uploadId") String uploadId,
            @RequestParam("key") String key
    ) {
        if (!canUploadProjectFiles(project_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if (!isProjectFileKeyValid(project_id, key)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid key");
        }

        List<UploadedPartDTO> parts = multipartService.listUploadedParts(key, uploadId);
        return ResponseEntity.ok(parts);
    }

    @PostMapping("/project/{project_id}/uploads/{uploadId}/complete")
    public ResponseEntity<?> completeProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @PathVariable String uploadId,
            @RequestBody CompleteUploadRequest request
    ) {
        if (!canUploadProjectFiles(project_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if (request == null || request.getKey() == null || request.getKey().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("key is required");
        }
        if (!isProjectFileKeyValid(project_id, request.getKey())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid key");
        }
        if (request.getParts() == null || request.getParts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("parts are required");
        }

        List<PartETag> etags = request.getParts().stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getEtag()))
                .collect(Collectors.toList());
        multipartService.completeMultipartUpload(request.getKey(), uploadId, etags);

        ProjectModel project = projectRepository.findByProjectId(project_id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such project");
        }

        ProjectFilesModel file = new ProjectFilesModel();
        file.setProject(project);
        file.setS3key(request.getKey());
        file.setFile_name(resolveProjectFileName(request.getFileName(), request.getKey()));
        projectFilesRepository.save(file);

        FileDTO dto = projectFileToDto(file);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/project/{project_id}/uploads/{uploadId}/abort")
    public ResponseEntity<?> abortProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @PathVariable String uploadId,
            @RequestParam("key") String key
    ) {
        if (!canUploadProjectFiles(project_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if (!isProjectFileKeyValid(project_id, key)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid key");
        }
        multipartService.abortMultipartUpload(key, uploadId);
        return ResponseEntity.ok("Upload aborted");
    }

    @GetMapping("/project/{project_id}")
    public ResponseEntity<?> getProjectFiles(@PathVariable("project_id") Integer project_id) {
        ProjectModel project = projectRepository.findByProjectId(project_id);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such project");
        }

        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                project_id
        );
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }

        List<FileDTO> files = projectFilesRepository.findByProjectId(project_id).stream()
                .map(this::projectFileToDto)
                .toList();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/project/{project_id}/{file_id}/download")
    public ResponseEntity<?> getProjectFileDownloadUrl(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("file_id") Integer file_id
    ) {
        ProjectFilesModel file = projectFilesRepository.findByFileIdAndProjectId(file_id, project_id);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such file");
        }

        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                project_id
        );
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }

        String url = multipartService.generatePresignedDownloadUrl(file.getS3key(), URL_LIFESPAN);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/project/{project_id}/{file_id}")
    public ResponseEntity<?> deleteProjectFile(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("file_id") Integer file_id
    ) {
        if (!canUploadProjectFiles(project_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        ProjectFilesModel file = projectFilesRepository.findByFileIdAndProjectId(file_id, project_id);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such file");
        }

        multipartService.deleteFile(file.getS3key());
        projectFilesRepository.delete(file);
        return ResponseEntity.ok("deleted");
    }

    @GetMapping("/{asset_id}/getVersions")
    public ResponseEntity<?> getVersions(@PathVariable("asset_id") Integer asset_id){
        List<FileMetadataModel> versionFiles = fileMetadataRepository.findVersionsByAssetId(asset_id);
        if(versionFiles == null || versionFiles.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("asset not found");
        }
        Integer taskId = versionFiles.get(0).getTask().getTask_id();
        Boolean isVerifierOrExecutor = taskRepository.getExecutors(taskId).contains(Auth.user()) ||
                taskRepository.getVerifiers(taskId).contains(Auth.user());
        if(!isVerifierOrExecutor){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }

        List<FileDTO> files = versionFiles.stream().map(file ->{
            FileDTO dto = new FileDTO();
            dto.setFile_id(file.getFile_id());
            dto.setFile_name(file.getFile_name());
            dto.setTask_id(file.getTask().getTask_id());
            dto.setStatus(file.getStatus());
            dto.setCreated_at(file.getCreated_at());
            dto.setUpdated_at(file.getUpdated_at());
            dto.setUpload_id(file.getUpload_id());
            dto.setS3Key(file.getS3key());
            log.info(dto.getS3Key());
            dto.setVerifier_file(taskRepository.getVerifiers(file.getTask().getTask_id()).contains(file.getUser()));
            dto.setUser(file.getUser().getUserDTO());
            dto.setVersion(file.getVersion());
            dto.setAsset_id(file.getAsset_id());
            return dto;
        }).toList();



        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{project_id}/delete")
    public ResponseEntity<?> deleteFile(@RequestBody FileDTO file, @PathVariable("project_id") Integer project_id){
        boolean isCreator = ProjectRolePermissions.canCreateTask(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), project_id));
        log.info(file.getS3Key());
        if(file.getS3Key()== null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("empty key" + file.getFile_name());
        }
        FileMetadataModel fileOrig = fileMetadataRepository.findByFileS3Key(file.getS3Key());

        if(!Objects.equals(fileOrig.getUser().getEmail(), Auth.user().getEmail()) && !isCreator){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights" + isCreator + " " + fileOrig.getUser().getEmail());
        }

        multipartService.deleteAnnotationsForFile(fileOrig.getFile_id());
        multipartService.deleteFile(file.getS3Key());
        fileMetadataRepository.delete(fileMetadataRepository.findByFileS3Key(file.getS3Key()));
        return ResponseEntity.ok("deleted");
    }
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getDownloadUrl(@PathVariable Integer fileId) {
        FileMetadataModel file = fileMetadataRepository.findByFileId(fileId);

        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such file");
        }

        Integer projectId = file.getTask().getProject().getProject_id();
        if (userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(), projectId) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        String url = multipartService.generatePresignedDownloadUrl(file.getS3key(), URL_LIFESPAN);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/uploads/{uploadId}/abort")
    public ResponseEntity<String> abortUpload(@PathVariable String uploadId) {
        FileMetadataModel file = fileMetadataRepository.findByUploadId(uploadId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such upload");
        }

        Integer projectId = file.getTask().getProject().getProject_id();
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                projectId
        );

        if (roles == null || !ProjectRolePermissions.canCreateTask(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        multipartService.abortMultipartUpload(file.getS3key(), uploadId);


        fileMetadataRepository.delete(file);

        return ResponseEntity.ok("Upload aborted");
    }


    @GetMapping("/uploads/{uploadId}/parts")
    public ResponseEntity<?> getUploadedParts(@PathVariable String uploadId) {
        FileMetadataModel file = fileMetadataRepository.findByUploadId(uploadId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such upload");
        }

        Integer projectId = file.getTask().getProject().getProject_id();
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                projectId
        );

        if (roles == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
        }

        List<UploadedPartDTO> parts = multipartService.listUploadedParts(
                file.getS3key(),
                uploadId
        );

        return ResponseEntity.ok(parts);
    }

    @GetMapping("/{file_id}/annotations/get")
    public ResponseEntity<?> getAnnotations(
            @PathVariable("file_id") Integer file_id,
            @RequestParam(name = "include_payload", defaultValue = "true") boolean includePayload){
        FileMetadataModel file = fileMetadataRepository.findByFileId(file_id);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such file");
        }
        Integer task_id = file.getTask().getTask_id();
        if(!taskServices.canAddComments(task_id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        var annotations = fileAnnotationsRepository.getAnnotationsByFileId(file_id);
        if (includePayload) {
            return ResponseEntity.ok(multipartService.modelsToDTO(annotations));
        }
        return ResponseEntity.ok(multipartService.modelsToMetaDTO(annotations));
    }

    @GetMapping("/annotations/{annotation_id}/download")
    public ResponseEntity<?> getAnnotationDownloadUrl(@PathVariable("annotation_id") Integer annotation_id) {
        var annotation = fileAnnotationsRepository.findByAnnotationId(annotation_id);
        if (annotation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such annotation");
        }
        if (!taskServices.canAddComments(annotation.getFile().getTask().getTask_id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        String key = annotation.getS3Key();
        if (key == null || key.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("annotation payload is missing");
        }
        String url = multipartService.generatePresignedDownloadUrl(key, URL_LIFESPAN);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/annotations/{annotation_id}")
    public ResponseEntity<?> deleteAnnotation(@PathVariable("annotation_id") Integer annotation_id) {
        var annotation = fileAnnotationsRepository.findByAnnotationId(annotation_id);
        if (annotation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such annotation");
        }
        if (!taskServices.canAddComments(annotation.getFile().getTask().getTask_id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
        }
        if (annotation.getS3Key() != null && !annotation.getS3Key().isBlank()) {
            multipartService.deleteFile(annotation.getS3Key());
        }
        fileAnnotationsRepository.delete(annotation);
        return ResponseEntity.ok("annotation deleted");
    }

    @PostMapping("/annotations/add")
    public ResponseEntity<?> addAnnotations(@RequestBody List<AnnotationCreateRequest> items){

        if(items==null || items.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("items is empty");
        }

        return multipartService.createBatch(items);
    }

    private boolean canUploadProjectFiles(Integer projectId) {
        if (projectId == null) {
            return false;
        }
        List<ProjectRoles> roles = userProjectRepository.findRolesByUserAndProject(
                Auth.user().getUserId(),
                projectId
        );
        return roles != null && ProjectRolePermissions.canCreateTask(roles);
    }

    private boolean isProjectFileKeyValid(Integer projectId, String key) {
        if (projectId == null || key == null || key.isBlank()) {
            return false;
        }
        String expectedPrefix = "projects/" + projectId + "/";
        return key.startsWith(expectedPrefix);
    }

    private FileDTO projectFileToDto(ProjectFilesModel file) {
        FileDTO dto = new FileDTO();
        dto.setFile_id(file.getFile_id());
        dto.setFile_name(file.getFile_name());
        dto.setStatus(FileStatus.COMPLETED);
        dto.setS3Key(file.getS3key());
        return dto;
    }

    private String resolveProjectFileName(String requestFileName, String key) {
        if (requestFileName != null && !requestFileName.isBlank()) {
            return requestFileName.trim();
        }
        if (key == null || key.isBlank()) {
            return "project-file";
        }
        String raw = key.substring(key.lastIndexOf('/') + 1);
        int separatorIndex = raw.indexOf('_');
        if (separatorIndex >= 0 && separatorIndex < raw.length() - 1) {
            return raw.substring(separatorIndex + 1);
        }
        return raw;
    }



    @Data
    public static class InitiateUploadResponse {
        private final String upload_id;
        private final String key;
        private final Integer file_id;
    }

    @Data
    public static class CompleteUploadRequest {
        private String key;
        private String fileName;
        private List<PartInfo> parts;
    }

    @Data
    public static class PartInfo {
        private int partNumber;
        private String etag;
    }
    public record AnnotationCreateRequest(
            Integer fileId,
            String annotationType,
            String status,
            Object payload
    ) {}
}
