package com.example._d_task.s3.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

    @Value("${aws.public-endpoint:}")
    private String publicEndpoint;

    @Value("${aws.bucket}")
    private String bucket;

    @Bean("s3Client")
    public AmazonS3 s3Client(){
        return buildClient(endpoint);
    }

    @Bean("s3PresignClient")
    public AmazonS3 s3PresignClient() {
        String resolvedEndpoint =
            publicEndpoint == null || publicEndpoint.isBlank()
                ? endpoint
                : publicEndpoint;
        return buildClient(resolvedEndpoint);
    }

    private AmazonS3 buildClient(String endpointUrl) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl,region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    @Bean
    public String bucketName(){
        return bucket;
    }
}
