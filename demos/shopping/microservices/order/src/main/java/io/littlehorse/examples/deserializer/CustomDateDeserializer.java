// src/main/java/io/littlehorse/examples/dto/CustomDateDeserializer.java
package io.littlehorse.examples.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {
    private static final String PATTERN = "MMM dd, yyyy, h:mm:ss a";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(PATTERN);

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().replace('\u202F', ' ').replace('\u00A0', ' ');
        try {
            return FORMAT.parse(dateStr);
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + dateStr, e);
        }
    }
}