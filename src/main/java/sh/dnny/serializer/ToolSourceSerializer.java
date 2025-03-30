package sh.dnny.serializer;

import burp.api.montoya.core.ToolSource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ToolSourceSerializer extends JsonSerializer<ToolSource> {
    @Override
    public void serialize(ToolSource source, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("toolType", source.toolType());
        gen.writeEndObject();
    }
}