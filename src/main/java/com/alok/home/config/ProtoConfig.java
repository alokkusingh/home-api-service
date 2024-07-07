package com.alok.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@ImportRuntimeHints(ProtobufRuntimeHints.class)
@Configuration
public class ProtoConfig {
    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }
}
