package com.floodrescue.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.floodrescue.shared.util.TextNormalizationUtil;

import java.io.IOException;

public class TextNormalizationStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return TextNormalizationUtil.cleanDisplayText(parser.getValueAsString());
    }
}
