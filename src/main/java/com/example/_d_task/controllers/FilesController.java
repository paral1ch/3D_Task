package com.example._d_task.controllers;


import com.amazonaws.services.s3.model.PartETag;
import com.example._d_task.dto.UploadDTO;
import com.example._d_task.enums.FileStatus;
import com.example._d_task.enums.ProjectRolePermissions;
import com.example._d_task.models.FileMetadataModel;
import com.example._d_task.models.ProjectModel;
import com.example._d_task.repositories.FileMetadataRepository;
import com.example._d_task.repositories.ProjectRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.repositories.UserProjectRepository;
import com.example._d_task.security.Classes.Auth;
import com.example._d_task.services.MultipartService;
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
        return ResponseEntity.ok(new InitiateUploadResponse(uploadId,key,file.getFile_id()));
    }

    @GetMapping("/upload/{upload_id}/parts/{part_number}/{key}")
    public ResponseEntity<?> getPresignedURL(@PathVariable("upload_id") String upload_id,
                                             @PathVariable("part_number") Integer part_number,
                                             @PathVariable("key") String key){
        String url = multipartService.generatePresignedUrlForPart(key,upload_id,part_number,URL_LIFESPAN);

        return ResponseEntity.ok(url);
    }

    @PostMapping("/uploads/{uploadId}/complete")
    public ResponseEntity<Void> completeUpload(
            @PathVariable String uploadId,
            @RequestBody CompleteUploadRequest request) {

        List<PartETag> etags = request.getParts().stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getEtag()))
                .collect(Collectors.toList());

        multipartService.completeMultipartUpload(request.getKey(), uploadId, etags);

        FileMetadataModel metadata = fileMetadataRepository.findByUploadId(uploadId);
        metadata.setStatus(FileStatus.COMPLETED);
        fileMetadataRepository.save(metadata);

        return ResponseEntity.ok().build();
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
