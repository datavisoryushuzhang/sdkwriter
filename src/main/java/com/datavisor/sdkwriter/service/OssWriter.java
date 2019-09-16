/*************************************************************************
 *
 * Copyright (c) 2016, DATAVISOR, INC.
 * All rights reserved.
 * __________________
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of DataVisor, Inc.
 * The intellectual and technical concepts contained
 * herein are proprietary to DataVisor, Inc. and
 * may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from DataVisor, Inc.
 */

package com.datavisor.sdkwriter.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.datavisor.sdkwriter.config.SdkWriterProperties;
import com.datavisor.sdkwriter.util.SdkUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

public class OssWriter implements DvWriter {
    private static final Logger logger = LoggerFactory.getLogger(OssWriter.class);
    private static final String DEFAULT_BUCKET_KEY = "default";
    private static final String DEFAULT_BUCKET = "datavisor-clientless";

    private OSS ossClient;

    private ObjectMapper mapper;

    private SdkWriterProperties properties;

    public OssWriter(OSS ossClient, ObjectMapper mapper,
            SdkWriterProperties properties) {
        this.ossClient = ossClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public boolean write(String key, String value) {
        String[] keyFields = { properties.getRecord().getClientNameKey(),
                properties.getRecord().getAppNameKey(), properties.getRecord().getEventNameKey() };

        String bucketName = Optional.ofNullable(SdkUtil.getSdkBucketNameFromObjectKey(key,
                properties.getRecord().getKeyDelimiter()))
                .map(name -> properties.getBuckets().get(name))
                .orElse(properties.getBuckets().getOrDefault(DEFAULT_BUCKET_KEY, DEFAULT_BUCKET));
        if (!ossClient.doesBucketExist(bucketName)) {
            logger.info("created bucket: {}", bucketName);
            ossClient.createBucket(bucketName);
        }

        // get current object size
        long currentPosition = ossClient.doesObjectExist(bucketName, key) ?
                ossClient.getSimplifiedObjectMeta(bucketName, key).getSize() :
                0L;
        logger.info("put object {} to bucket: {}", key, bucketName);
        long start = System.currentTimeMillis();
        AppendObjectRequest request = new AppendObjectRequest(bucketName, key,
                new ByteArrayInputStream(value.getBytes()));
        request.setPosition(currentPosition);

        AppendObjectResult result = ossClient.appendObject(request);
        logger.info("tooks {} ms to append {}", System.currentTimeMillis() - start, key);
        return result != null;
    }
}
