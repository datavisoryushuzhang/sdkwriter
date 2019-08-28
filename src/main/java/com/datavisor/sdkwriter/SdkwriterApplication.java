package com.datavisor.sdkwriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@SpringBootApplication
public class SdkwriterApplication implements EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(SdkwriterApplication.class);
    private static final String CLIENT_PREFIX = "sdkwriter-";
    public static final String SPRING_KAFKA_CONSUMER_CLIENT_ID = "spring.kafka.streams.client_id";

    public static void main(String[] args) {
        SpringApplication.run(SdkwriterApplication.class, args);
    }

    @Override
    public void setEnvironment(Environment environment) {
        Properties props = new Properties();
        try {
            props.setProperty(
                    SPRING_KAFKA_CONSUMER_CLIENT_ID,
                    CLIENT_PREFIX + InetAddress.getLocalHost().getHostName());
            PropertiesPropertySource propertySource = new PropertiesPropertySource("myProps",
                    props);
            if (environment instanceof StandardEnvironment) {
                ((StandardEnvironment) environment).getPropertySources().addFirst(propertySource);
            }
        } catch (UnknownHostException e) {
            logger.warn("can not get local hostname, this may cause problem");
        }
    }
}
