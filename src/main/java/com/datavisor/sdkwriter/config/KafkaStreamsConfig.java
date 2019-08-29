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
import com.datavisor.sdkwriter.util.JsonUtil;
import com.datavisor.sdkwriter.util.SdkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private static final int IN_MEMORY_STORE_RETENCTION_TIME = 24 * 60;
    @Autowired
    private SdkWriterProperties sdkWriterProperties;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DvWriter writer;

    @Value("${sdkwriter.record.client_name_key}")
    private String clientNameKey;

    @Value("${sdkwriter.record.app_name_key}")
    private String appNameKey;

    @Value("${sdkwriter.record.event_name_key}")
    private String eventNameKey;

    @Bean
    public KStream<String, JsonNode> kafkaStream(StreamsBuilder builder) {
        KStream<String, JsonNode> records = builder.stream(sdkWriterProperties.getInputTopic());

        String[] sdkkeys = { clientNameKey, appNameKey, eventNameKey };
        records
                .filter(SdkUtil.validate(sdkkeys))
                .groupBy(
                        SdkUtil.buildSdkGroupKeys(sdkWriterProperties.getRecord().getKeyDelimiter(),
                                sdkkeys))
                // Windowed by 5 Minutes and wait another 30s for late arrive records
                .windowedBy(TimeWindows
                        .of(Duration.ofMinutes(sdkWriterProperties.getWindow().getWindowTime()))
                        .grace(Duration.ofSeconds(
                                sdkWriterProperties.getWindow().getWaitFor())))
                .aggregate(
                        // initializer
                        String::new,
                        // aggregator
                        (key, node, agg) -> {
                            String stringNode = JsonUtil.printJson(mapper, node);
                            StringBuilder stringBuilder = new StringBuilder();
                            return agg.isEmpty() ?
                                    stringNode :
                                    stringBuilder.append(agg).append("\n").append(stringNode)
                                            .toString();
                        },
                        // Custom state store
                        Materialized.<String, String>as(
                                Stores.inMemoryWindowStore("stream-agg-store",
                                        Duration.ofMinutes(IN_MEMORY_STORE_RETENCTION_TIME),
                                        Duration.ofMinutes(
                                                sdkWriterProperties.getWindow().getWindowTime()),
                                        false))
                                //                                .withLoggingDisabled()
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                )
                // Action interval settings
                .suppress(Suppressed.untilTimeLimit(
                        Duration.ofSeconds(sdkWriterProperties.getWindow().getWriterWindow()),
                        Suppressed.BufferConfig
                                .maxBytes(sdkWriterProperties.getWindow().getMemLimit())))
                // Write to file
                .toStream(SdkUtil.buildWindowedKey(sdkWriterProperties.getWindow().getDelimiter()))
                .peek((key, value) -> writer.write(key, value))
                // write to output stream
                .mapValues(String::length)
                .to(sdkWriterProperties.getOutputTopic(),
                        Produced.keySerde(Serdes.String()));

        return records;
    }

    //    @Bean
    //    public KTable<String, ArrayNode> outputStream(StreamsBuilder builder) {
    //        KTable<String, ArrayNode> outputs = builder.table(sdkWriterProperties.getOutputTopic());
    //
    //        outputs
    //
    //        return outputs;
    //    }
}
