package com.floodrescue.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.floodrescue.shared.util.TextNormalizationUtil;

import java.io.IOException;

public class TextNormalizationStringSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(TextNormalizationUtil.cleanDisplayText(value));
    }
}
