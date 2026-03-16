package com.studyhelper.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(AliyunOssProperties.class)
public class AliyunOssConfig {

    @Bean
    public OssClientProvider ossClientProvider(AliyunOssProperties properties, ObjectMapper objectMapper) throws IOException {
        OSS oss = null;
        if (properties.isEnabled()
                && properties.getEndpoint() != null && !properties.getEndpoint().isBlank()
                && properties.getBucket() != null && !properties.getBucket().isBlank()) {
            String secretFile = properties.getSecretFile();
            if (secretFile == null || secretFile.isBlank()) {
                secretFile = "./config/aliyun-secret.json";
            }

            File file = new File(secretFile);
            if (file.exists()) {
                AliyunSecret secret = objectMapper.readValue(file, AliyunSecret.class);
                if (secret.getAccessKeyId() != null && !secret.getAccessKeyId().isBlank()
                        && secret.getAccessKeySecret() != null && !secret.getAccessKeySecret().isBlank()) {
                    oss = new OSSClientBuilder().build(properties.getEndpoint(), secret.getAccessKeyId(), secret.getAccessKeySecret());
                }
            }
        }
        return new OssClientProvider(oss, properties);
    }
}

