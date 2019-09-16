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

package com.datavisor.sdkwriter.config;

import com.aliyun.oss.OSS;
import com.datavisor.sdkwriter.service.DvWriter;
import com.datavisor.sdkwriter.service.OssWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DvWrtierConfig {
    private static final Logger logger = LoggerFactory.getLogger(DvWrtierConfig.class);

    @Autowired
    private ObjectMapper mapper;

    @Bean
    @ConditionalOnProperty(name = "aliyun.endpoint")
    public DvWriter ossWriter(OSS ossClient, SdkWriterProperties properties) {
        return new OssWriter(ossClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean(DvWriter.class)
    public DvWriter defaultDvWriter() {
        return (key, value) -> {
            try {
                logger.info("key: {}, value: \n{}", key,
                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                logger.error("error process json", e);
            }
            return false;
        };
    }
}
