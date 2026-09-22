package com.example._d_task.controllers;


import com.example._d_task.dto.FileDTO;
import com.example._d_task.dto.UploadDTO;
import com.example._d_task.services.FilesService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/files")
public class FilesController {
    private final FilesService filesService;

    public FilesController(
            FilesService filesService
    ){
        this.filesService = filesService;
    }


    private static final Logger log = Logger.getLogger(
            FilesController.class.getName()
    );

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<?> initiateUpload(
            @RequestBody UploadDTO uploadDTO){
        return filesService.initiateUpload(uploadDTO);
    }

    @GetMapping("/upload/{upload_id}/parts/{part_number}")
    public ResponseEntity<?> getPresignedURL(@PathVariable("upload_id") String upload_id,
                                             @PathVariable("part_number") Integer part_number){
        return filesService.getPresignedURL(upload_id,part_number);
    }

    @PostMapping("/uploads/{uploadId}/complete")
    public ResponseEntity<String> completeUpload(
            @PathVariable String uploadId,
            @RequestBody FilesService.CompleteUploadRequest request) {
        return filesService.completeUpload(uploadId,request);
    }


    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTaskFiles(@PathVariable Integer taskId) {
        return filesService.getTaskFiles(taskId);
    }

    @PostMapping("/project/{project_id}/upload")
    public ResponseEntity<?> initiateProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @RequestBody UploadDTO uploadDTO
    ) {
        return filesService.initiateProjectUpload(project_id,uploadDTO);
    }

    @GetMapping("/project/{project_id}/upload/{upload_id}/parts/{part_number}")
    public ResponseEntity<?> getProjectPresignedPartUrl(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("upload_id") String upload_id,
            @PathVariable("part_number") Integer part_number,
            @RequestParam("key") String key
    ) {
        return filesService.getProjectPresignedPartUrl(project_id,upload_id,part_number,key);
    }

    @GetMapping("/project/{project_id}/uploads/{uploadId}/parts")
    public ResponseEntity<?> getProjectUploadedParts(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("uploadId") String uploadId,
            @RequestParam("key") String key
    ) {
        return filesService.getProjectUploadedParts(project_id,uploadId,key);
    }



    @PostMapping("/project/{project_id}/uploads/{uploadId}/complete")
    public ResponseEntity<?> completeProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @PathVariable String uploadId,
            @RequestBody FilesService.CompleteUploadRequest request
    ) {
        return filesService.completeProjectUpload(project_id,uploadId,request);
    }

    @PostMapping("/project/{project_id}/uploads/{uploadId}/abort")
    public ResponseEntity<?> abortProjectUpload(
            @PathVariable("project_id") Integer project_id,
            @PathVariable String uploadId,
            @RequestParam("key") String key
    ) {
        return filesService.abortProjectUpload(project_id,uploadId,key);
    }

    @GetMapping("/project/{project_id}")
    public ResponseEntity<?> getProjectFiles(@PathVariable("project_id") Integer project_id) {
        return filesService.getProjectFiles(project_id);
    }

    @GetMapping("/project/{project_id}/{file_id}/download")
    public ResponseEntity<?> getProjectFileDownloadUrl(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("file_id") Integer file_id
    ) {
        return filesService.getProjectFileDownloadUrl(project_id,file_id);
    }



    @DeleteMapping("/project/{project_id}/{file_id}")
    public ResponseEntity<?> deleteProjectFile(
            @PathVariable("project_id") Integer project_id,
            @PathVariable("file_id") Integer file_id
    ) {
        return filesService.deleteProjectFile(project_id,file_id);
    }

    @GetMapping("/{asset_id}/getVersions")
    public ResponseEntity<?> getVersions(@PathVariable("asset_id") Integer asset_id){
        return filesService.getVersions(asset_id);
    }

    @DeleteMapping("/{project_id}/delete")
    public ResponseEntity<?> deleteFile(@RequestBody FileDTO file, @PathVariable("project_id") Integer project_id){
        return filesService.deleteFile(file,project_id);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> getDownloadUrl(@PathVariable Integer fileId) {
        return filesService.getDownloadUrl(fileId);
    }

    @PostMapping("/uploads/{uploadId}/abort")
    public ResponseEntity<String> abortUpload(@PathVariable String uploadId) {
        return filesService.abortUpload(uploadId);
    }


    @GetMapping("/uploads/{uploadId}/parts")
    public ResponseEntity<?> getUploadedParts(@PathVariable String uploadId) {
        return filesService.getUploadedParts(uploadId);
    }

    @GetMapping("/{file_id}/annotations/get")
    public ResponseEntity<?> getAnnotations(
            @PathVariable("file_id") Integer file_id,
            @RequestParam(name = "include_payload", defaultValue = "true") boolean includePayload){
        return filesService.getAnnotations(file_id,includePayload);
    }

    @GetMapping("/annotations/{annotation_id}/download")
    public ResponseEntity<?> getAnnotationDownloadUrl(@PathVariable("annotation_id") Integer annotation_id) {
        return filesService.getAnnotationDownloadUrl(annotation_id);
    }

    @DeleteMapping("/annotations/{annotation_id}")
    public ResponseEntity<?> deleteAnnotation(@PathVariable("annotation_id") Integer annotation_id) {
        return filesService.deleteAnnotation(annotation_id);
    }

    @PostMapping("/annotations/add")
    public ResponseEntity<?> addAnnotations(@RequestBody List<FilesService.AnnotationCreateRequest> items){
        return filesService.addAnnotations(items);
    }
}
