package com.floodrescue.config.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module textNormalizationModule() {
        SimpleModule module = new SimpleModule("text-normalization");
        module.addSerializer(String.class, new TextNormalizationStringSerializer());
        module.addDeserializer(String.class, new TextNormalizationStringDeserializer());
        return module;
    }
}
