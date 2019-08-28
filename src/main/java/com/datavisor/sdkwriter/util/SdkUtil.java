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
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SdkUtil {

    private static final String WINDOW_SPLIT = "-";
    private static final String WINDOW_TIME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String rawlogPrefix = "rawlog.";

    /**
     * Validate sdk event
     *
     * @param keys the key fields must have
     * @return A {@link Predicate} with given key fields
     */
    public static Predicate<String, ? super JsonNode> validate(String... keys) {
        return (k, v) -> {
            for (String key : keys) {
                if (!v.hasNonNull(key)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Group a sdk message with the given key fields
     *
     * @param keys the key fields
     * @return
     */
    public static KeyValueMapper<String, ? super JsonNode, String> buildSdkGroupKeys(
            String delimiter, String... keys) {
        return (msgKey, jsondata) -> Stream.of(keys)
                .map(jsondata::get)
                .map(JsonNode::asText)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Build the windowed key
     *
     * @return
     */
    public static KeyValueMapper<Windowed<String>, ? super JsonNode, String> buildWindowedKey(
            String windowDelimiter) {
        return (key, value) -> {
            String startTime = DateTimeFormatter.ofPattern(WINDOW_TIME_FORMAT)
                    .format(key.window().startTime().atOffset(ZoneOffset.UTC));
            String endTime = DateTimeFormatter.ofPattern(WINDOW_TIME_FORMAT)
                    .format(key.window().endTime().atOffset(ZoneOffset.UTC));
            return key.key() + windowDelimiter + startTime + WINDOW_SPLIT + endTime;
        };
    }

    public static Map<String, String> parseSdkGroupKeys(String key, String delimiter,
            String windowDelimiter, String... keyFields) {
        System.out.println(key);
        String[] fields = key.split(windowDelimiter)[0].split(delimiter);
        Map<String, String> keys = new HashMap<>();
        keys.put(keyFields[0], fields[0]);
        keys.put(keyFields[1], fields[1]);
        keys.put(keyFields[2], fields[2]);

        return keys;
    }

    public static String buildObjectName(String key, String windowDelimiter,
            String sdkFolder, String consumerId) {
        String[] clientKey = key.split(windowDelimiter);
        String objectTimestamp = clientKey[1].split(WINDOW_SPLIT)[0];
        String objectSuffix = clientKey[0].replaceAll("/", "_");

        return sdkFolder + "/" + objectTimestamp.split("_")[0] + "/" + rawlogPrefix
                + objectTimestamp + "." + objectSuffix + "." + consumerId;
    }
}
