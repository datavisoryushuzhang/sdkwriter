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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SdkUtil {

    private static final String DELIMITER = "_";

    @Value("${sdkwriter.record.client_name_key}")
    private static String clientNameKey;

    @Value("${sdkwriter.record.app_name_key}")
    private static String appNameKey;

    @Value("${sdkwriter.record.event_name_key}")
    private static String eventNameKey;

    public static Function<JsonNode, Boolean> validate(String... keys) {
        return jsondata -> {
            for (String key : keys) {
                if (!jsondata.hasNonNull(key)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static BiFunction<String, JsonNode, String> buildSdkGroupKeys(String... keys) {
        return (msgKey, jsondata) -> Stream.of(keys)
                .map(jsondata::get)
                .map(JsonNode::asText)
                .collect(Collectors.joining(DELIMITER));
    }

    public static Map<String, String> parseSdkGroupKeys(String key) {
        String[] fields = key.split(DELIMITER);
        Map<String, String> keys = new HashMap<>();
        keys.put(clientNameKey, fields[0]);
        keys.put(appNameKey, fields[1]);
        keys.put(eventNameKey, fields[2]);

        return keys;
    }
}
