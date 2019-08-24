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

import com.datavisor.sdkwriter.service.DvWriter;
import com.datavisor.sdkwriter.util.SdkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Autowired
    private SdkWriterProperties sdkWriterProperties;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DvWriter writer;

    @Bean
    public KStream<String, JsonNode> kafkaStream(StreamsBuilder builder) {
        KStream<String, JsonNode> records = builder.stream(sdkWriterProperties.getInputTopic());

        String[] sdkkeys = { sdkWriterProperties.getRecord().getClientNameKey(),
                sdkWriterProperties.getRecord().getAppNameKey(),
                sdkWriterProperties.getRecord().getEventNameKey() };
        records
                .filter((key, jsondata) -> SdkUtil.validate(sdkkeys).apply(jsondata))
                .groupBy((key, jsonData) -> SdkUtil.buildSdkGroupKeys(sdkkeys).apply(key, jsonData))
                // Windowed by 5 Minutes and wait another 30s for late arrive records
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).grace(Duration.ofSeconds(30)))
                .aggregate(
                        // initializer
                        mapper::createArrayNode,
                        // aggregator
                        (key, node, agg) -> agg.add(node)
                )
                // write to output kafka
                .toStream((key, value) -> key.key() + "@" + DateTimeFormatter.ISO_DATE_TIME
                        .format(key.window().startTime().atOffset(ZoneOffset.UTC)) + "-" +
                        DateTimeFormatter.ISO_DATE_TIME
                                .format(key.window().endTime().atOffset(ZoneOffset.UTC)))
                .to(sdkWriterProperties.getOutputTopic(),
                        Produced.keySerde(Serdes.String()));

        return records;
    }

    @Bean
    public KTable<String, ArrayNode> outputStream(StreamsBuilder builder) {
        KTable<String, ArrayNode> outputs = builder.table(sdkWriterProperties.getOutputTopic());

        outputs
                // Action interval: 5min,  Only reserve the latest record in each key
                .suppress(Suppressed.untilTimeLimit(Duration.ofMinutes(1),
                        Suppressed.BufferConfig.maxRecords(24)))
                //                // Write to file
                .toStream()
                .foreach((key, value) -> writer.write(key, value));

        return outputs;
    }
}
