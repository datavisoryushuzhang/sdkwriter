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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
    public static <V> KeyValueMapper<Windowed<String>, V, String> buildWindowedKey(
            String windowDelimiter) {
        return (key, value) -> {

            Instant startTimeInstant = key.window().startTime();
            Instant endTimeInstant = key.window().endTime();

            String startTime = DateTimeFormatter.ofPattern(WINDOW_TIME_FORMAT)
                    .format(startTimeInstant.atOffset(ZoneOffset.UTC));
            String endTime = DateTimeFormatter.ofPattern(WINDOW_TIME_FORMAT)
                    .format(endTimeInstant.atOffset(ZoneOffset.UTC));
            return key.key() + windowDelimiter + startTime + WINDOW_SPLIT + endTime;
        };
    }

    public static String getSdkBucketNameFromObjectKey(String objectKey, String delimiter) {
        System.out.println(objectKey);
        return Optional.of(objectKey)
                //split the full path: /rawdata_sdk/2019XXXX/XXXXXXXXXXXXx
                .map(k -> k.split("/", 3)).map(fullPath -> SdkUtil.safeGet(fullPath, 2))
                // split the filename: rawlog.2019XXXX_XXXXXX.XXXXXXXX
                .map(n -> n.split("\\.", 3)).map(fileName -> SdkUtil.safeGet(fileName, 2))
                // split client key: xxxxxxx_xxxx_xxxx
                .map(c -> c.split(delimiter)).map(clientKey -> SdkUtil.safeGet(clientKey, 0))
                .orElse(null);
    }

    public static String buildObjectName(Windowed<String> key, String sdkFolder, long timestamp,
            long windowTime) {
        String objectTimestamp = DateTimeFormatter.ofPattern(WINDOW_TIME_FORMAT)
                .format(Instant.ofEpochMilli(Math.floorDiv(timestamp, windowTime) * windowTime)
                        .atOffset(ZoneOffset.UTC));
        String objectSuffix = key.key().replaceAll("/", "-");

        return sdkFolder + "/" + objectTimestamp.split("_")[0] + "/" + rawlogPrefix
                + objectTimestamp + "." + objectSuffix + "." + Thread.currentThread().getName();
    }

    private static String safeGet(String[] strings, int index) {
        return strings.length <= index ? null : strings[index];
    }
}
