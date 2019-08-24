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

import com.datavisor.sdkwriter.config.SdkWriterProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DvWriterImpl implements DvWriter {
    private static final Logger logger = LoggerFactory.getLogger(DvWriter.class);

    @Autowired
    private SdkWriterProperties properties;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public boolean write(String key, JsonNode value) {
        try {
            logger.info("key: {}, value: \n{}", key,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
        } catch (JsonProcessingException e) {
            logger.error("error process json", e);
        }
        return false;
    }
}
