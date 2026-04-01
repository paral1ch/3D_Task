package com.example._d_task.controllers;


import com.amazonaws.services.s3.model.PartETag;
import com.example._d_task.dto.FileDTO;
import com.example._d_task.dto.UploadDTO;
import com.example._d_task.dto.UploadedPartDTO;
import com.example._d_task.enums.FileStatus;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.enums.ProjectRoles;
import com.example._d_task.models.FileMetadataModel;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.repositories.FileMetadataRepository;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.MultipartService;
import com.example._d_task.services.TaskServices;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FilesController {

    @Autowired
    private MultipartService multipartService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserProjectRepository userProjectRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskServices taskServices;

    private final Integer URL_LIFESPAN = 15;

    @PostMapping("/upload")
    public ResponseEntity<?> initiateUpload(
            @RequestBody UploadDTO uploadDTO){
        ProjectModel pm = projectRepository.findByTaskId(uploadDTO.getTaskId());

        if(pm==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no such project");
        }

        if(userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                pm.getProject_id())==null || !ProjectRolePermissions.canCreateTask(
                userProjectRepository.findRolesByUserAndProject(Auth.user().getUserId(),
                        pm.getProject_id())
        )){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You dont have rights");
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
            return dto;
        }).toList();

        return ResponseEntity.ok(files);
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

    @Data
    public static class InitiateUploadResponse {
        private final String upload_id;
        private final String key;
        private final Integer file_id;
    }

    @Data
    public static class CompleteUploadRequest {
        private String key;
        private List<PartInfo> parts;
    }

    @Data
    public static class PartInfo {
        private int partNumber;
        private String etag;
    }

}
