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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sdkwriter")
public class SdkWriterProperties {
    private Record record;
    private String inputTopic;
    private String outputTopic;

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public static class Record {
        private String appNameKey;
        private String eventNameKey;

        public String getClientNameKey() {
            return clientNameKey;
        }

        public void setClientNameKey(String clientNameKey) {
            this.clientNameKey = clientNameKey;
        }

        private String clientNameKey;

        public String getAppNameKey() {
            return appNameKey;
        }

        public void setAppNameKey(String appNameKey) {
            this.appNameKey = appNameKey;
        }

        public String getEventNameKey() {
            return eventNameKey;
        }

        public void setEventNameKey(String eventNameKey) {
            this.eventNameKey = eventNameKey;
        }
    }
}
