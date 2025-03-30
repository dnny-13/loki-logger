package sh.dnny.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import sh.dnny.model.LogLine;

import java.io.IOException;

public class LogLineSerializer extends JsonSerializer<LogLine> {
    @Override
    public void serialize(LogLine logLine, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("source", logLine.getSource());
        gen.writeObjectField("request", logLine.getRequest());
        gen.writeObjectField("response", logLine.getResponse());
        gen.writeNumberField("responseTime", logLine.getResponseTime());
        gen.writeEndObject();
    }
}