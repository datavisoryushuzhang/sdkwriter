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
import com.aliyun.oss.model.PutObjectResult;
import com.datavisor.sdkwriter.config.SdkWriterProperties;
import com.datavisor.sdkwriter.util.JsonUtil;
import com.datavisor.sdkwriter.util.SdkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import static com.datavisor.sdkwriter.util.SdkUtil.buildObjectName;

@Service
@Profile("aliyun")
public class OssWriter implements DvWriter {
    private static final Logger logger = LoggerFactory.getLogger(OssWriter.class);
    private static final String DEFAULT_BUCKET_KEY = "default";
    private static final String DEFAULT_BUCKET = "datavisor-clientless";

    @Autowired
    private OSS ossClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SdkWriterProperties properties;

    @Override
    public boolean write(String key, JsonNode value) {
        String[] keyFields = { properties.getRecord().getClientNameKey(),
                properties.getRecord().getAppNameKey(), properties.getRecord().getEventNameKey() };
        Map<String, String> keys = SdkUtil
                .parseSdkGroupKeys(key, properties.getRecord().getKeyDelimiter(),
                        properties.getWindow().getDelimiter(), keyFields);
        String clientName = keys.get(properties.getRecord().getClientNameKey());

        String bucketName = Optional.ofNullable(clientName)
                .map(name -> properties.getBuckets().get(name))
                .orElse(properties.getBuckets().getOrDefault(DEFAULT_BUCKET_KEY, DEFAULT_BUCKET));
        if (!ossClient.doesBucketExist(bucketName)) {
            logger.info("created bucket: {}", bucketName);
            ossClient.createBucket(bucketName);
        }

        String objectName = buildObjectName(key, properties.getWindow().getDelimiter(),
                properties.getSdkFolder(), Thread.currentThread().getName());

        logger.info("put object {} to bucket: {}", objectName, bucketName);
        long start = System.currentTimeMillis();
        PutObjectResult result = ossClient.putObject(bucketName, objectName,
                new ByteArrayInputStream(JsonUtil.printJsonPerLine(mapper, value).getBytes()));
        logger.info("tooks {} ms to upload {}", System.currentTimeMillis() - start, objectName);
        return result != null;
    }

}
