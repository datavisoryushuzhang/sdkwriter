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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "sdkwriter")
@RefreshScope
public class SdkWriterProperties {
    private Record record;
    private String inputTopic;
    private String outputTopic;
    private Window window;
    private String sdkFolder;

    public String getSdkFolder() {
        return sdkFolder;
    }

    public void setSdkFolder(String sdkFolder) {
        this.sdkFolder = sdkFolder;
    }

    private Map<String, String> buckets;

    public Map<String, String> getBuckets() {
        return buckets;
    }

    public void setBuckets(Map<String, String> buckets) {
        this.buckets = buckets;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

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
        private String keyDelimiter;

        public String getKeyDelimiter() {
            return keyDelimiter;
        }

        public void setKeyDelimiter(String keyDelimiter) {
            this.keyDelimiter = keyDelimiter;
        }

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

    public static class Window {
        private int windowTime;
        private int waitFor;
        private int writerWindow;
        private long memLimit;

        public String getDelimiter() {
            return delimiter;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        private String delimiter = "@";

        public int getWindowTime() {
            return windowTime;
        }

        public void setWindowTime(int windowTime) {
            this.windowTime = windowTime;
        }

        public int getWaitFor() {
            return waitFor;
        }

        public void setWaitFor(int waitFor) {
            this.waitFor = waitFor;
        }

        public int getWriterWindow() {
            return writerWindow;
        }

        public void setWriterWindow(int writerWindow) {
            this.writerWindow = writerWindow;
        }

        public long getMemLimit() {
            return memLimit;
        }

        public void setMemLimit(long memLimit) {
            this.memLimit = memLimit;
        }
    }

    @PostConstruct
    public void init() {
        System.out.format("buckets: %s", getBuckets());
    }
}
