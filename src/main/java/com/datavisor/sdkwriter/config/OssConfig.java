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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "aliyun.endpoint")
public class OssConfig {
    @Value("${aliyun.endpoint}")
    private String endpoint;

    @Value("${aliyun.access_id}")
    private String accessId;

    @Value("${aliyun.access_key}")
    private String accessKey;

    @Bean
    public OssClientFactory ossClientFactory() {
        final OssClientFactory factoryBean = new OssClientFactory();
        factoryBean.setEndpoint(endpoint);
        factoryBean.setAccessKeyId(accessId);
        factoryBean.setAccessKeySecret(accessKey);
        return factoryBean;
    }
}
