package com.ezann.files.uploader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${aws-access-key-id}")
    private String awsAccessKeyId;

    @Value("${aws-secret-key}")
    private String awsSecretKey;

    @Value("${aws-bucket-region}")
    private String awsBucketRegion;

    @Bean
    public S3Client s3ClientProvider() {
        Region region = Region.of(awsBucketRegion);
        AwsCredentials credentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey);

        return S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
