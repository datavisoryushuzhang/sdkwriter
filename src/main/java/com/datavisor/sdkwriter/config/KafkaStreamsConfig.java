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
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class);

    @Autowired
    private SdkWriterProperties sdkWriterProperties;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DvWriter writer;

    @Bean
    public KStream<String, JsonNode> kafkaStream(StreamsBuilder builder) {
        KStream<String, JsonNode> records = builder.stream(sdkWriterProperties.getInputTopic());

        records
                .filter(SdkUtil.validate(sdkWriterProperties.getRecord().getClientNameKey(),
                        sdkWriterProperties.getRecord().getAppNameKey(),
                        sdkWriterProperties.getRecord().getEventNameKey()))
                .groupBy(
                        SdkUtil.buildSdkGroupKeys(sdkWriterProperties.getRecord().getKeyDelimiter(),
                                sdkWriterProperties.getRecord().getClientNameKey(),
                                sdkWriterProperties.getRecord().getAppNameKey(),
                                sdkWriterProperties.getRecord().getEventNameKey()))
                // Windowed by 5 Minutes and wait another 30s for late arrive records
                .windowedBy(TimeWindows
                        .of(Duration.ofSeconds(sdkWriterProperties.getWindow().getWriterWindow()))
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
                                        Duration.ofSeconds(
                                                sdkWriterProperties.getWindow().getWriterWindow()),
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
                .toStream()
                .transformValues(WriteToBuckets::new)
                // write to output stream
                .mapValues(String::length)
                .selectKey(SdkUtil.buildWindowedKey(sdkWriterProperties.getWindow().getDelimiter()))
                .to(sdkWriterProperties.getOutputTopic(),
                        Produced.keySerde(Serdes.String()));

        return records;
    }

    public class WriteToBuckets
            implements ValueTransformerWithKey<Windowed<String>, String, String> {
        private ProcessorContext context;

        @Override public void init(ProcessorContext context) {
            this.context = context;
        }

        @Override public String transform(Windowed<String> readOnlyKey,
                String value) {
            String key = SdkUtil.buildObjectName(readOnlyKey,
                    sdkWriterProperties.getSdkFolder(), context.timestamp(),
                    Duration.ofSeconds(
                            sdkWriterProperties.getWindow().getWindowTime())
                            .toMillis());

            writer.write(key, value);
            return value;
        }

        @Override public void close() {

        }
    }
}
