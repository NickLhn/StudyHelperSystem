package com.studyhelper.config;

import com.aliyun.oss.OSS;

public class OssClientProvider implements AutoCloseable {
    private final OSS oss;
    private final AliyunOssProperties properties;

    public OssClientProvider(OSS oss, AliyunOssProperties properties) {
        this.oss = oss;
        this.properties = properties;
    }

    public boolean isAvailable() {
        return oss != null;
    }

    public OSS getOss() {
        return oss;
    }

    public AliyunOssProperties getProperties() {
        return properties;
    }

    @Override
    public void close() {
        if (oss != null) {
            oss.shutdown();
        }
    }
}

