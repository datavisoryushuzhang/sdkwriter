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

package com.datavisor.sdkwriter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.StreamSupport;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public static String prettyPrintJson(ObjectMapper mapper, JsonNode value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("error print json: {}", value);
        }
        return "";
    }

    public static String printJson(ObjectMapper mapper, JsonNode value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("error print json: {}", value);
        }
        return "";
    }

    public static String printJsonPerLine(ObjectMapper mapper, JsonNode value) {
        if (ArrayNode.class.isAssignableFrom(value.getClass())) {
            ArrayNode arrayValue = (ArrayNode) value;
            return StreamSupport.stream(arrayValue.spliterator(), false)
                    .map(node -> {
                        try {
                            return mapper.writeValueAsString(node);
                        } catch (JsonProcessingException e) {
                            logger.error("error print json: {}", node);
                        }
                        return "";
                    })
                    .reduce("", (prev, next) -> prev + "\n" + next);
        } else {
            return printJson(mapper, value);
        }
    }
}
