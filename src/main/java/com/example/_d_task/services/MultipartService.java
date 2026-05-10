package com.example._d_task.services;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example._d_task.controllers.FilesController;
import com.example._d_task.dto.FileAnnotationDTO;
import com.example._d_task.dto.FileDTO;
import com.example._d_task.dto.UploadedPartDTO;
import com.example._d_task.models.FileAnnotationsModel;
import com.example._d_task.models.FileMetadataModel;
import com.example._d_task.repositories.FileAnnotationsRepository;
import com.example._d_task.repositories.FileMetadataRepository;
import com.example._d_task.repositories.TaskRepository;
import com.example._d_task.security.Classes.Auth;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class MultipartService {
    private final AmazonS3 s3Client;
    private final AmazonS3 s3PresignClient;
    private final String bucketName;
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileAnnotationsRepository fileAnnotationsRepository;

    @Autowired
    private TaskServices taskServices;

    @Autowired
    private TaskRepository taskRepository;

    private static final Logger log = Logger.getLogger(
            MultipartService.class.getName()
    );
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    public MultipartService(
            @Qualifier("s3Client") AmazonS3 s3Client,
            @Qualifier("s3PresignClient") AmazonS3 s3PresignClient,
            @Qualifier("bucketName") String bucketName
    ) {
        this.s3Client = s3Client;
        this.s3PresignClient = s3PresignClient;
        this.bucketName = bucketName;
    }


    public String initiateMultipartUpload(String key, String contentType){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, key)
                .withObjectMetadata(metadata)
                .withCannedACL(CannedAccessControlList.Private);

        InitiateMultipartUploadResult result = s3Client.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    public String generatePresignedUrlForPart(String key, String uploadId, int partNumber, int expirationMinutes){

        Date expiration =  new Date(System.currentTimeMillis() + expirationMinutes * 60_000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName,key)

                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType("application/octet-stream");
        request.addRequestParameter("uploadId",uploadId);
        request.addRequestParameter("partNumber",String.valueOf(partNumber));
        return s3PresignClient.generatePresignedUrl(request).toString();
    }

    public void completeMultipartUpload(String key, String uploadId, List<PartETag> parts){
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, key, uploadId, parts);
        s3Client.completeMultipartUpload(request);
    }
    public void abortMultipartUpload(String key, String uploadId){
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName,key,uploadId);
        s3Client.abortMultipartUpload(request);
    }


    public String generatePresignedDownloadUrl(String key, int expirationMinutes) {
        Date expiration = new Date(System.currentTimeMillis() + expirationMinutes * 60_000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return s3PresignClient.generatePresignedUrl(request).toString();
    }

    public List<UploadedPartDTO> listUploadedParts(String key, String uploadId) {
        List<UploadedPartDTO> result = new ArrayList<>();

        ListPartsRequest request = new ListPartsRequest(bucketName, key, uploadId);
        PartListing listing;

        do {
            listing = s3Client.listParts(request);

            for (PartSummary part : listing.getParts()) {
                result.add(
                        new UploadedPartDTO(
                                part.getPartNumber(),
                                part.getETag(),
                                part.getSize()
                        )
                );
            }

            if (listing.isTruncated()) {
                request.setPartNumberMarker(listing.getNextPartNumberMarker());
            }
        } while (listing.isTruncated());

        return result;
    }

    public ResponseEntity<String> deleteFile(String s3Key){
        log.info(s3Key);
        DeleteObjectRequest request = new DeleteObjectRequest(bucketName,s3Key);
        log.info(s3Key);
        s3Client.deleteObject(request);
        return ResponseEntity.ok("Complete");
    }

    public FileDTO modelToDTO(FileMetadataModel file){
        FileDTO dto = new FileDTO();

        dto.setUser(file.getUser().getUserDTO());
        dto.setS3Key(file.getS3key());
        dto.setStatus(file.getStatus());
        dto.setTask_id(file.getTask().getTask_id());
        dto.setFile_id(file.getFile_id());
        dto.setCreated_at(file.getCreated_at());
        dto.setFile_name(file.getFile_name());
        return dto;
    }



    public FileAnnotationDTO modelToDTO(FileAnnotationsModel file){
        FileAnnotationDTO dto = new FileAnnotationDTO();

        dto.setId(file.getId());
        dto.setS3Key(file.getS3Key());
        dto.setFile(modelToDTO(file.getFile()));
        dto.setPayload(loadPayloadFromS3(file.getS3Key()));
        dto.setCreated_at(file.getCreated_at());
        dto.setCreated_by(file.getCreated_by().getUserDTO());
        return dto;
    }

    public FileAnnotationDTO modelToMetaDTO(FileAnnotationsModel file){
        FileAnnotationDTO dto = new FileAnnotationDTO();

        dto.setId(file.getId());
        dto.setS3Key(file.getS3Key());
        dto.setFile(modelToDTO(file.getFile()));
        dto.setPayload(null);
        dto.setCreated_at(file.getCreated_at());
        dto.setCreated_by(file.getCreated_by().getUserDTO());
        return dto;
    }

    public List<FileAnnotationDTO> modelsToDTO(List<FileAnnotationsModel> files){
        List<FileAnnotationDTO> dtos =new ArrayList<>();

        for(FileAnnotationsModel file: files){
            dtos.add(modelToDTO(file));
        }
        return dtos;
    }

    public List<FileAnnotationDTO> modelsToMetaDTO(List<FileAnnotationsModel> files){
        List<FileAnnotationDTO> dtos =new ArrayList<>();

        for(FileAnnotationsModel file: files){
            dtos.add(modelToMetaDTO(file));
        }
        return dtos;
    }


    public ResponseEntity<?> createBatch(List<FilesController.AnnotationCreateRequest> items){

        List<FileAnnotationsModel> annotations = new ArrayList<>();
        for(FilesController.AnnotationCreateRequest item: items){
            if (item == null || item.fileId() == null || item.payload() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid annotation payload");
            }
            FileMetadataModel file = fileMetadataRepository.findByFileId(item.fileId());
            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("file not found: " + item.fileId());
            }
            Boolean isVerifier = !taskRepository.getVerifiers(file.getTask().getTask_id()).contains(Auth.user());
            if(isVerifier){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you dont have rights");
            }
            FileAnnotationsModel annotation = new FileAnnotationsModel();
            annotation.setCreated_at(LocalDate.now());
            annotation.setCreated_by(Auth.user());
            String annotationKey;
            try {
                annotationKey = uploadPayloadToS3(file, item.payload());
            } catch (Exception exception) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("failed to upload annotation payload");
            }
            annotation.setS3Key(annotationKey);
            annotation.setFile(file);
            fileAnnotationsRepository.save(annotation);
            annotations.add(annotation);
        }

        return ResponseEntity.ok(modelsToDTO(annotations));
    }

    public void deleteAnnotationsForFile(Integer fileId) {
        if (fileId == null) {
            return;
        }
        List<FileAnnotationsModel> annotations = fileAnnotationsRepository.getAnnotationsByFileId(fileId);
        for (FileAnnotationsModel annotation : annotations) {
            if (annotation.getS3Key() == null || annotation.getS3Key().isBlank()) {
                continue;
            }
            try {
                s3Client.deleteObject(new DeleteObjectRequest(bucketName, annotation.getS3Key()));
            } catch (Exception exception) {
                log.warning("Failed to delete annotation object from S3: " + annotation.getS3Key());
            }
        }
    }

    private String uploadPayloadToS3(FileMetadataModel file, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        String key = String.format(
            "tasks/%d/annotations/%d/%s.json",
            file.getTask().getTask_id(),
            file.getFile_id(),
            UUID.randomUUID()
        );
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        metadata.setContentLength(bytes.length);
        PutObjectRequest request = new PutObjectRequest(
            bucketName,
            key,
            new ByteArrayInputStream(bytes),
            metadata
        ).withCannedAcl(CannedAccessControlList.Private);
        s3Client.putObject(request);
        return key;
    }

    private Object loadPayloadFromS3(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }
        try (S3Object object = s3Client.getObject(bucketName, s3Key)) {
            byte[] bytes = IOUtils.toByteArray(object.getObjectContent());
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            log.warning("Failed to load annotation payload from S3 key: " + s3Key);
            return null;
        }
    }

}
