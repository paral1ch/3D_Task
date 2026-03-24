package com.example._d_task.services;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MultipartService {
    private final AmazonS3 s3Client;
    private final String bucketName;

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
        return s3Client.generatePresignedUrl(request).toString();
    }

    public void completeMultipartUpload(String key, String uploadId, List<PartETag> parts){
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, key, uploadId, parts);
        s3Client.completeMultipartUpload(request);
    }
    public void abortMultipartUpload(String key, String uploadId){
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName,key,uploadId);
        s3Client.abortMultipartUpload(request);
    }

}
